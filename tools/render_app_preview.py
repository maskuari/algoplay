from pathlib import Path
from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "docs" / "previews"
OUT.mkdir(parents=True, exist_ok=True)

W, H = 390, 844

COLORS = {
    "blue": "#29A9EC",
    "blue_dark": "#1589D1",
    "blue_soft": "#EAF8FF",
    "bg": "#F1FAFF",
    "text": "#14324A",
    "subtext": "#62798D",
    "yellow": "#FFD84D",
    "green": "#62DEB5",
    "green_dark": "#2BCB7A",
    "purple": "#8C72FF",
    "pink": "#FF7BB5",
    "white": "#FFFFFF",
    "orange": "#FFB457",
}


def font(size, bold=False):
    candidates = [
        Path("C:/Windows/Fonts/arialbd.ttf" if bold else "C:/Windows/Fonts/arial.ttf"),
        Path("C:/Windows/Fonts/segoeuib.ttf" if bold else "C:/Windows/Fonts/segoeui.ttf"),
    ]
    for path in candidates:
        if path.exists():
            return ImageFont.truetype(str(path), size)
    return ImageFont.load_default(size=size)


F = {
    "xs": font(10),
    "sm": font(12),
    "body": font(14),
    "body_b": font(14, True),
    "md": font(16),
    "md_b": font(16, True),
    "lg": font(20, True),
    "xl": font(26, True),
    "hero": font(32, True),
}


def draw_text(draw, xy, text, fill, fnt, max_width=None, line_gap=4, anchor=None, align="left"):
    if max_width is None:
        draw.text(xy, text, fill=fill, font=fnt, anchor=anchor, align=align)
        return

    words = text.split()
    lines = []
    current = ""
    for word in words:
        trial = word if not current else f"{current} {word}"
        if draw.textbbox((0, 0), trial, font=fnt)[2] <= max_width:
            current = trial
        else:
            if current:
                lines.append(current)
            current = word
    if current:
        lines.append(current)

    x, y = xy
    for line in lines:
        draw.text((x, y), line, fill=fill, font=fnt, align=align)
        y += fnt.size + line_gap


def rr(draw, box, radius=18, fill=COLORS["white"], outline=None, width=1):
    draw.rounded_rectangle(box, radius=radius, fill=fill, outline=outline, width=width)


def circle(draw, center, radius, fill):
    x, y = center
    draw.ellipse((x - radius, y - radius, x + radius, y + radius), fill=fill)


def paste_asset(img, name, box):
    path = ROOT / "app" / "src" / "main" / "res" / "drawable" / name
    asset = Image.open(path).convert("RGBA")
    x1, y1, x2, y2 = box
    asset.thumbnail((x2 - x1, y2 - y1), Image.Resampling.LANCZOS)
    ax = x1 + ((x2 - x1) - asset.width) // 2
    ay = y1 + ((y2 - y1) - asset.height) // 2
    img.alpha_composite(asset, (ax, ay))


def base(bg=COLORS["bg"]):
    img = Image.new("RGBA", (W, H), bg)
    draw = ImageDraw.Draw(img)
    circle(draw, (390, 95), 55, "#C9F7EB")
    circle(draw, (-10, 180), 38, "#FFF1A8")
    return img, draw


def top_phone(draw, title=""):
    draw.rectangle((0, 0, W, 26), fill="#FFFFFF")
    draw.text((24, 8), "9:41", fill=COLORS["text"], font=F["xs"])
    draw.text((W - 78, 8), "WiFi 100%", fill=COLORS["text"], font=F["xs"])
    if title:
        draw.text((24, 40), title, fill=COLORS["text"], font=F["xl"])


def button(draw, box, text, fill=COLORS["blue"], fg=COLORS["white"]):
    rr(draw, box, 18, fill)
    x1, y1, x2, y2 = box
    draw.text(((x1 + x2) / 2, (y1 + y2) / 2), text, fill=fg, font=F["md_b"], anchor="mm")


def field(draw, y, label, value):
    draw.text((42, y), label, fill=COLORS["text"], font=F["body_b"])
    rr(draw, (42, y + 26, 348, y + 80), 16, "#F8FCFF", "#D6E8F4")
    draw.text((58, y + 44), value, fill=COLORS["subtext"], font=F["body"])


def nav(draw, active):
    rr(draw, (24, 746, 366, 820), 28, COLORS["white"])
    items = [("Home", "H"), ("Materi", "M"), ("Latihan", "L"), ("Rank", "R"), ("Profil", "P")]
    x = 37
    for label, code in items:
        is_active = label == active
        if is_active:
            rr(draw, (x - 8, 758, x + 48, 808), 18, COLORS["blue_soft"])
        col = COLORS["blue_dark"] if is_active else COLORS["subtext"]
        circle(draw, (x + 20, 775), 9, col)
        draw.text((x + 20, 795), label, fill=col, font=F["xs"], anchor="mm")
        x += 67


def header(draw, title, subtitle):
    draw.text((24, 42), title, fill=COLORS["text"], font=F["xl"])
    draw_text(draw, (24, 75), subtitle, COLORS["subtext"], F["body"], 250)
    rr(draw, (292, 40, 354, 102), 20, COLORS["white"])
    draw.text((323, 71), "A", fill=COLORS["blue_dark"], font=F["xl"], anchor="mm")


def hero(draw, title, body, chip, color=COLORS["blue"]):
    rr(draw, (24, 124, 366, 270), 24, color)
    circle(draw, (302, 160), 34, "#FFFFFF33")
    circle(draw, (335, 230), 20, "#FFFFFF33")
    draw.text((46, 148), title, fill=COLORS["white"], font=F["lg"])
    draw_text(draw, (46, 180), body, "#EAF8FF", F["body"], 210)
    rr(draw, (46, 222, 170, 250), 14, COLORS["white"])
    draw.text((108, 236), chip, fill=COLORS["blue_dark"], font=F["sm"], anchor="mm")


def mini_card(draw, box, title, desc, color):
    rr(draw, box, 18, color)
    x1, y1, x2, y2 = box
    circle(draw, (x1 + 28, y1 + 32), 18, "#FFFFFFAA")
    draw.text((x1 + 54, y1 + 20), title, fill=COLORS["text"], font=F["md_b"])
    draw_text(draw, (x1 + 54, y1 + 46), desc, COLORS["subtext"], F["sm"], x2 - x1 - 68)


def progress(draw, y, title, value):
    rr(draw, (24, y, 366, y + 92), 20, COLORS["white"])
    draw.text((44, y + 20), title, fill=COLORS["text"], font=F["md_b"])
    draw.text((335, y + 20), f"{value}%", fill=COLORS["green_dark"], font=F["body_b"], anchor="ra")
    rr(draw, (44, y + 56, 346, y + 68), 6, "#DDECF6")
    rr(draw, (44, y + 56, 44 + int(302 * value / 100), y + 68), 6, COLORS["green_dark"])


def splash():
    img = Image.new("RGBA", (W, H), COLORS["blue"])
    d = ImageDraw.Draw(img)
    circle(d, (195, 350), 100, "#FFFFFF25")
    circle(d, (330, 105), 38, COLORS["yellow"])
    circle(d, (70, 170), 24, COLORS["green"])
    circle(d, (322, 615), 28, COLORS["pink"])
    paste_asset(img, "logodepan.png", (58, 236, 332, 438))
    d.text((195, 462), "Belajar Algoritma & Flowchart Jadi Seru!", fill=COLORS["white"], font=F["body_b"], anchor="mm")
    rr(d, (112, 500, 278, 532), 16, "#FFF5B8")
    d.text((195, 516), "Siap berpetualang?", fill=COLORS["text"], font=F["sm"], anchor="mm")
    rr(d, (113, 760, 277, 770), 6, "#DDF6FF")
    rr(d, (113, 760, 242, 770), 6, COLORS["yellow"])
    d.text((195, 796), "V.0.0.1", fill="#DDF6FF", font=F["sm"], anchor="mm")
    return img


def login(register=False):
    img, d = base()
    top_phone(d)
    rr(d, (24, 46, 366, 120), 22, COLORS["blue"])
    paste_asset(img, "logodepan.png", (48, 58, 120, 108))
    d.text((132, 70), "AlgoPlay", fill=COLORS["white"], font=F["lg"])
    d.text((132, 96), "Buat akun belajar baru" if register else "Belajar Algoritma dan Flowchart", fill="#EAF8FF", font=F["sm"])
    rr(d, (24, 146, 366, 632 if register else 600), 24, COLORS["white"])
    d.text((195, 180), "Buat Akun" if register else "Masuk akun", fill=COLORS["text"], font=F["lg"], anchor="mm")
    draw_text(d, (60, 208), "Simpan progres, bintang, dan reward belajarmu." if register else "Gunakan email atau Google untuk membuka progres belajarmu.", COLORS["subtext"], F["sm"], 270)
    y = 246
    if register:
        field(d, y, "Nama", "Masukkan nama")
        y += 96
    field(d, y, "Email", "contoh@email.com")
    y += 96
    field(d, y, "Password", "Minimal 6 karakter" if register else "Masukkan password")
    y += 100
    button(d, (42, y, 348, y + 54), "Daftar" if register else "Masuk")
    if not register:
        d.text((195, y + 84), "atau", fill=COLORS["subtext"], font=F["sm"], anchor="mm")
        rr(d, (42, y + 108, 348, y + 160), 18, COLORS["white"], "#D6E8F4")
        d.text((195, y + 134), "G  Masuk dengan Google", fill=COLORS["text"], font=F["md_b"], anchor="mm")
    rr(d, (42, 676, 348, 724), 18, "#FFFFFFCC")
    d.text((165, 700), "Sudah punya akun?" if register else "Belum punya akun?", fill=COLORS["subtext"], font=F["body"], anchor="mm")
    d.text((260, 700), "Masuk" if register else "Daftar", fill=COLORS["blue_dark"], font=F["body_b"], anchor="mm")
    return img


def onboarding():
    img, d = base()
    top_phone(d)
    rr(d, (136, 56, 254, 88), 16, "#FFF5B8")
    d.text((195, 72), "Halo, Teman!", fill=COLORS["text"], font=F["sm"], anchor="mm")
    rr(d, (58, 128, 332, 402), 42, COLORS["white"])
    paste_asset(img, "logo.png", (78, 148, 312, 382))
    d.text((195, 466), "Welcome to AlgoPlay!", fill=COLORS["text"], font=F["xl"], anchor="mm")
    draw_text(d, (44, 500), "Aplikasi belajar algoritma dan flowchart untuk anak-anak lewat materi mini, puzzle, kuis, reward, dan petualangan seru.", COLORS["subtext"], F["body"], 302, align="center")
    button(d, (28, 748, 362, 806), "Mulai Belajar")
    return img


def home():
    img, d = base()
    paste_asset(img, "logodepan.png", (24, 34, 196, 100))
    rr(d, (300, 44, 360, 104), 22, COLORS["white"])
    d.text((330, 74), "P", fill=COLORS["blue_dark"], font=F["xl"], anchor="mm")
    rr(d, (24, 126, 366, 314), 24, "#CFF3FF")
    d.text((46, 152), "Halo,", fill=COLORS["subtext"], font=F["lg"])
    d.text((46, 184), "Ayo Belajar!", fill=COLORS["text"], font=F["hero"])
    draw_text(d, (46, 232), "Yuk lanjutkan petualangan belajar hari ini!", COLORS["subtext"], F["body"], 195)
    paste_asset(img, "logodepan.png", (214, 180, 360, 300))
    rr(d, (24, 334, 366, 472), 20, COLORS["white"])
    circle(d, (72, 402), 34, COLORS["blue_soft"])
    d.text((112, 364), "Lanjutkan Belajar", fill=COLORS["text"], font=F["lg"])
    d.text((112, 394), "Dasar Flowchart", fill=COLORS["blue_dark"], font=F["md_b"])
    rr(d, (112, 424, 256, 436), 6, "#DDECF6")
    rr(d, (112, 424, 220, 436), 6, COLORS["green_dark"])
    button(d, (270, 378, 346, 432), "Lanjut", COLORS["green_dark"])
    d.text((24, 502), "Aktivitas Seru", fill=COLORS["text"], font=F["lg"])
    mini_card(d, (24, 532, 184, 626), "Materi", "Belajar konsep dasar", "#DDF2FF")
    mini_card(d, (206, 532, 366, 626), "Flowchart", "Pahami alur program", "#DFF8EA")
    mini_card(d, (24, 640, 184, 734), "Games", "Belajar sambil main", "#EFE8FF")
    mini_card(d, (206, 640, 366, 734), "Rank", "Lihat peringkatmu", "#FFF1CF")
    nav(d, "Home")
    return img


def materi():
    img, d = base()
    top_phone(d)
    header(d, "Materi Belajar", "Buka materi berurutan dari awal sampai ujian akhir.")
    hero(d, "Jalur Belajar", "Selesaikan satu materi untuk membuka materi berikutnya.", "1/12 Selesai")
    d.text((24, 292), "Daftar Materi", fill=COLORS["text"], font=F["lg"])
    rows = [
        ("1", "Yuk Kenalan dengan Algoritma", "Selesai"),
        ("2", "Algoritma Itu Seperti Langkah-langkah", "Terbuka"),
        ("", "Belajar Urutan Kegiatan", "Terkunci"),
        ("", "Menulis Langkah dengan Kata-kata Sendiri", "Terkunci"),
        ("", "Mengenal Pseudocode dengan Mudah", "Terkunci"),
    ]
    y = 322
    for num, title, status in rows:
        unlocked = status != "Terkunci"
        rr(d, (24, y, 366, y + 70), 18, COLORS["white"] if unlocked else "#EEF2F5")
        circle(d, (54, y + 35), 21, COLORS["blue_dark"] if unlocked else "#A7B6C2")
        d.text((54, y + 35), num or "L", fill=COLORS["white"], font=F["body_b"], anchor="mm")
        draw_text(d, (84, y + 15), title, COLORS["text"], F["body_b"], 180)
        d.text((84, y + 46), "Ketuk untuk belajar" if unlocked else "Selesaikan materi sebelumnya dulu", fill=COLORS["subtext"], font=F["xs"])
        d.text((342, y + 34), status, fill=COLORS["blue_dark"] if unlocked else COLORS["subtext"], font=F["xs"], anchor="rm")
        y += 80
    progress(d, 650, "Materi selesai", 8)
    nav(d, "Materi")
    return img


def latihan():
    img, d = base()
    top_phone(d)
    header(d, "Latihan Seru", "Asah logika lewat puzzle dan flowchart.")
    hero(d, "Susun Flowchart", "Cocokkan simbol, susun urutan, lalu kumpulkan bintang dan score.", "0/4 Mode", COLORS["green_dark"])
    d.text((24, 292), "Mode Latihan", fill=COLORS["text"], font=F["lg"])
    mini_card(d, (24, 326, 184, 436), "Puzzle Simbol", "Cocokkan bentuk", "#DFF8EA")
    mini_card(d, (206, 326, 366, 436), "Urutan Langkah", "Susun cerita", "#DDF2FF")
    mini_card(d, (24, 454, 184, 564), "Quiz Cepat", "Jawab pilihan", "#EFE8FF")
    mini_card(d, (206, 454, 366, 564), "Tantangan", "Dapat reward", "#FFF1CF")
    rr(d, (24, 588, 366, 706), 20, COLORS["white"])
    d.text((44, 608), "Puzzle Flowchart", fill=COLORS["text"], font=F["lg"])
    draw_text(d, (44, 638), "Cocokkan simbol ke fungsi yang benar. Reward: +2 bintang dan +120 score.", COLORS["subtext"], F["body"], 280)
    button(d, (44, 672, 346, 714), "Main Puzzle", COLORS["green_dark"])
    nav(d, "Latihan")
    return img


def leaderboard():
    img, d = base()
    top_phone(d)
    header(d, "Leaderboard", "Lihat peringkat dan special score terbaik.")
    hero(d, "Papan Peringkat", "Score kamu 0. Naikkan dari materi, puzzle, quiz, dan tantangan.", "Top 4", COLORS["orange"])
    d.text((24, 292), "Peringkat Minggu Ini", fill=COLORS["text"], font=F["lg"])
    rows = [
        ("#1 Raden", "2560 score", COLORS["yellow"]),
        ("#2 Albi", "2340 score", COLORS["green"]),
        ("#3 Teman", "1920 score", COLORS["purple"]),
        ("#4 Kamu", "0 score", COLORS["blue_soft"]),
    ]
    y = 326
    for name, score, color in rows:
        rr(d, (24, y, 366, y + 82), 20, COLORS["white"])
        circle(d, (62, y + 41), 24, color)
        d.text((98, y + 20), name, fill=COLORS["text"], font=F["md_b"])
        d.text((98, y + 48), score, fill=COLORS["subtext"], font=F["body"])
        y += 96
    progress(d, 650, "Total special score", 0)
    nav(d, "Rank")
    return img


def profil():
    img, d = base()
    paste_asset(img, "logodepan.png", (24, 34, 196, 100))
    rr(d, (300, 44, 360, 104), 22, COLORS["white"])
    d.text((330, 74), "P", fill=COLORS["blue_dark"], font=F["xl"], anchor="mm")
    rr(d, (24, 126, 366, 278), 24, "#CFF3FF")
    d.text((46, 150), "Halo, Teman!", fill=COLORS["subtext"], font=F["lg"])
    d.text((46, 184), "Level 1", fill=COLORS["text"], font=F["hero"])
    draw_text(d, (46, 232), "0 bintang dan 0 score. Terus lanjutkan misi!", COLORS["subtext"], F["body"], 210)
    circle(d, (315, 198), 45, COLORS["white"])
    d.text((315, 198), "A", fill=COLORS["blue_dark"], font=F["xl"], anchor="mm")
    rr(d, (24, 302, 366, 452), 20, COLORS["white"])
    d.text((44, 324), "Progres Belajar", fill=COLORS["text"], font=F["lg"])
    d.text((44, 354), "Selesai 0 dari 12 materi", fill=COLORS["subtext"], font=F["body"])
    rr(d, (44, 394, 250, 408), 7, "#DDECF6")
    rr(d, (44, 394, 62, 408), 7, COLORS["green_dark"])
    circle(d, (300, 378), 46, COLORS["blue_soft"])
    d.text((300, 378), "0%\nSelesai", fill=COLORS["blue_dark"], font=F["md_b"], anchor="mm", align="center")
    rr(d, (24, 476, 366, 608), 20, COLORS["white"])
    d.text((44, 498), "Statistik", fill=COLORS["text"], font=F["lg"])
    mini_card(d, (44, 530, 180, 590), "0%", "Akurasi Benar", "#DFF8EA")
    mini_card(d, (210, 530, 346, 590), "0", "Total Reward", "#FFF1CF")
    rr(d, (24, 630, 366, 724), 20, COLORS["white"])
    d.text((44, 652), "Badge", fill=COLORS["text"], font=F["lg"])
    d.text((44, 688), "Pemula Algoritma", fill=COLORS["blue_dark"], font=F["md_b"])
    nav(d, "Profil")
    return img


SCREENS = [
    ("01_splash", splash),
    ("02_login", login),
    ("03_register", lambda: login(True)),
    ("04_onboarding", onboarding),
    ("05_home", home),
    ("06_materi", materi),
    ("07_latihan", latihan),
    ("08_leaderboard", leaderboard),
    ("09_profil", profil),
]


def save_all():
    paths = []
    for name, fn in SCREENS:
        img = fn().convert("RGB")
        path = OUT / f"{name}.png"
        img.save(path, quality=95)
        paths.append(path)

    scale = 0.54
    thumb_w, thumb_h = int(W * scale), int(H * scale)
    gap = 18
    label_h = 30
    cols = 3
    rows = 3
    montage = Image.new(
        "RGB",
        (cols * thumb_w + (cols + 1) * gap, rows * (thumb_h + label_h) + (rows + 1) * gap),
        "#E8F5FC",
    )
    md = ImageDraw.Draw(montage)
    for i, path in enumerate(paths):
        img = Image.open(path).resize((thumb_w, thumb_h), Image.Resampling.LANCZOS)
        row, col = divmod(i, cols)
        x = gap + col * (thumb_w + gap)
        y = gap + row * (thumb_h + label_h + gap)
        md.text((x, y), path.stem.replace("_", " ").title(), fill=COLORS["text"], font=F["sm"])
        montage.paste(img, (x, y + label_h))
    montage_path = OUT / "algoplay_full_preview.png"
    montage.save(montage_path, quality=95)
    print(montage_path)
    for path in paths:
        print(path)


if __name__ == "__main__":
    save_all()
