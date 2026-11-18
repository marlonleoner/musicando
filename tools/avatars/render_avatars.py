"""
Gera as imagens dos personagens (avatares) do Musicando a partir do pacote "Mini Characters" da Kenney (CC0).

Para cada personagem são gerados, em cada pasta de saída:
  <id>-idle.webp   animação parada em loop (corpo inteiro)
  <id>-cheer.webp  pose de comemoração (corpo inteiro, imagem parada)
  <id>-face.webp   só o rosto, para placares e listas

Uso (veja o README.md desta pasta):
  python render_avatars.py --pack kenney_mini-characters.zip \
      --out ../../frontend/host-app/public/avatars --out ../../frontend/player-app/public/avatars
"""
import argparse
import math
import os
import shutil
import sys
import tempfile
import zipfile

import bpy
import mathutils
from PIL import Image

# Personagens usados no jogo. A mesma lista existe no backend (AvatarValidator) e no app do celular (avatars.ts).
CHARACTERS = [
    "alien", "female-a", "female-b", "female-c", "female-d", "female-e", "female-f", "knight", "male-a", "male-b",
    "male-c", "male-d", "male-e", "male-f", "male-g", "raider", "robot", "soldier", "worker", "zombie",
]

SIZE = 256
FACE_SIZE = 128
IDLE_STEP = 2
IDLE_FPS = 24 / IDLE_STEP
CHEER_ACTION, CHEER_FRAME = "jump", 6
CAMERA_SCALE = 1.0
CAMERA_YAW, CAMERA_PITCH = math.radians(22), math.radians(10)
SAMPLES = 16
WEBP_QUALITY = 85


def models_dir(pack, workdir):
    if os.path.isdir(pack):
        root = pack
    else:
        with zipfile.ZipFile(pack) as archive:
            archive.extractall(workdir)
        root = workdir

    path = os.path.join(root, "Models", "GLB format")
    if not os.path.isdir(path):
        sys.exit(f"Pasta 'Models/GLB format' não encontrada em {pack}")

    return path


def clear_scene():
    for collection in (bpy.data.objects, bpy.data.meshes, bpy.data.armatures, bpy.data.actions, bpy.data.materials,
                       bpy.data.images, bpy.data.cameras, bpy.data.lights, bpy.data.worlds):
        for item in list(collection):
            collection.remove(item)


def setup_render(scene):
    scene.render.engine = "CYCLES"
    scene.cycles.device = "CPU"
    scene.cycles.samples = SAMPLES
    scene.cycles.use_denoising = True
    scene.view_settings.view_transform = "Standard"
    scene.render.resolution_x = SIZE
    scene.render.resolution_y = SIZE
    scene.render.film_transparent = True


def use_action(armature, name):
    action = bpy.data.actions[name]
    armature.animation_data.action = action
    if action.slots:
        armature.animation_data.action_slot = action.slots[0]

    return action


def mesh_bounds():
    depsgraph = bpy.context.evaluated_depsgraph_get()
    points = []
    for obj in bpy.data.objects:
        if obj.type == "MESH":
            evaluated = obj.evaluated_get(depsgraph)
            mesh = evaluated.to_mesh()
            points += [evaluated.matrix_world @ vertex.co for vertex in mesh.vertices]
            evaluated.to_mesh_clear()

    low = mathutils.Vector([min(point[i] for point in points) for i in range(3)])
    high = mathutils.Vector([max(point[i] for point in points) for i in range(3)])

    return low, high


def add_camera_and_lights(scene, low, high):
    # Câmera ortográfica com a mesma escala para todos os personagens; os pés ficam sempre na mesma altura.
    target = (low + high) / 2
    target.z = low.z + CAMERA_SCALE * 0.46
    camera_data = bpy.data.cameras.new("camera")
    camera_data.type = "ORTHO"
    camera_data.ortho_scale = CAMERA_SCALE
    camera = bpy.data.objects.new("camera", camera_data)
    scene.collection.objects.link(camera)
    scene.camera = camera
    distance = 10
    camera.location = target + mathutils.Vector((
        math.sin(CAMERA_YAW) * math.cos(CAMERA_PITCH) * distance,
        -math.cos(CAMERA_YAW) * math.cos(CAMERA_PITCH) * distance,
        math.sin(CAMERA_PITCH) * distance))
    camera.rotation_euler = (target - camera.location).to_track_quat("-Z", "Y").to_euler()

    for name, energy, rotation in (("key", 3.2, (50, 10, 30)), ("fill", 1.2, (60, 0, -140))):
        light = bpy.data.lights.new(name, "SUN")
        light.energy = energy
        light_object = bpy.data.objects.new(name, light)
        scene.collection.objects.link(light_object)
        light_object.rotation_euler = tuple(math.radians(value) for value in rotation)

    world = bpy.data.worlds.new("world")
    scene.world = world
    world.use_nodes = True
    world.node_tree.nodes["Background"].inputs[0].default_value = (1, 1, 1, 1)
    world.node_tree.nodes["Background"].inputs[1].default_value = 0.6


def render_frame(scene, frame, path):
    scene.frame_set(frame)
    scene.render.filepath = path
    bpy.ops.render.render(write_still=True)

    return Image.open(path).convert("RGBA")


def render_character(name, models, frames_dir):
    clear_scene()
    scene = bpy.context.scene
    bpy.ops.import_scene.gltf(filepath=os.path.join(models, f"character-{name}.glb"))
    for obj in list(bpy.data.objects):
        # O modelo traz uma esfera auxiliar que não faz parte do personagem.
        if obj.name.startswith("Icosphere"):
            bpy.data.objects.remove(obj)

    armature = next(obj for obj in bpy.data.objects if obj.type == "ARMATURE")
    for track in armature.animation_data.nla_tracks:
        track.mute = True

    setup_render(scene)
    idle = use_action(armature, "idle")
    scene.frame_set(0)
    bpy.context.view_layer.update()
    add_camera_and_lights(scene, *mesh_bounds())

    start, end = int(idle.frame_range[0]), int(idle.frame_range[1])
    idle_frames = [render_frame(scene, frame, os.path.join(frames_dir, f"{name}-idle-{frame:03d}.png"))
                   for frame in range(start, end, IDLE_STEP)]
    use_action(armature, CHEER_ACTION)
    cheer = render_frame(scene, CHEER_FRAME, os.path.join(frames_dir, f"{name}-cheer.png"))

    return idle_frames, cheer


def face_of(image):
    # Quadrado na parte de cima do personagem (a cabeça), a partir da área não transparente da imagem.
    left, top, right, bottom = image.getchannel("A").getbbox()
    side = round((bottom - top) * 0.62)
    center = (left + right) / 2
    top = max(0, top - round(side * 0.04))
    box = (round(center - side / 2), top, round(center + side / 2), top + side)

    return image.crop(box).resize((FACE_SIZE, FACE_SIZE), Image.LANCZOS)


def save_outputs(name, idle_frames, cheer, outputs):
    for output in outputs:
        os.makedirs(output, exist_ok=True)
        idle_frames[0].save(os.path.join(output, f"{name}-idle.webp"), save_all=True, append_images=idle_frames[1:],
                            duration=round(1000 / IDLE_FPS), loop=0, quality=WEBP_QUALITY, method=6)
        cheer.save(os.path.join(output, f"{name}-cheer.webp"), quality=WEBP_QUALITY, method=6)
        face_of(idle_frames[0]).save(os.path.join(output, f"{name}-face.webp"), quality=WEBP_QUALITY, method=6)


def main():
    argv = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else sys.argv[1:]
    parser = argparse.ArgumentParser(description="Gera as imagens dos personagens do Musicando.")
    parser.add_argument("--pack", required=True, help="zip (ou pasta extraída) do pacote Mini Characters da Kenney")
    parser.add_argument("--out", required=True, action="append", help="pasta de saída (pode repetir)")
    parser.add_argument("--only", nargs="*", help="gera só estes personagens")
    args = parser.parse_args(argv)

    names = args.only or CHARACTERS
    unknown = sorted(set(names) - set(CHARACTERS))
    if unknown:
        sys.exit(f"Personagens desconhecidos: {', '.join(unknown)}")

    workdir = tempfile.mkdtemp(prefix="musicando-avatars-")
    try:
        models = models_dir(args.pack, workdir)
        frames_dir = os.path.join(workdir, "frames")
        os.makedirs(frames_dir)
        for index, name in enumerate(names, start=1):
            print(f"[{index}/{len(names)}] {name}", flush=True)
            idle_frames, cheer = render_character(name, models, frames_dir)
            save_outputs(name, idle_frames, cheer, args.out)
    finally:
        shutil.rmtree(workdir, ignore_errors=True)


if __name__ == "__main__":
    main()
