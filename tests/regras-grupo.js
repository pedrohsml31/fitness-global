/* Teste automático das regras de grupo muscular (GRP_REGRAS do index.html).
   Rodar:  node tests/regras-grupo.js
   Sai com código 1 se algum nome comum cair no grupo errado. Rode antes de publicar
   qualquer mudança em GRP_REGRAS. Para cobrir um erro novo, acrescente o nome na tabela. */
const fs = require("fs"), path = require("path");
const src = fs.readFileSync(path.join(__dirname, "..", "index.html"), "utf8");
const bloco = src.match(/const GRP_REGRAS = \[[\s\S]*?\n\];/);
if(!bloco){ console.error("GRP_REGRAS não encontrado no index.html"); process.exit(1); }
const norm = s => String(s || "").toLowerCase().normalize("NFD").replace(/[̀-ͯ]/g, "").replace(/[^a-z0-9\s-]/g, " ").replace(/\s+/g, " ").trim();
const GRP_REGRAS = new Function(bloco[0].replace("const GRP_REGRAS", "var GRP_REGRAS") + "; return GRP_REGRAS;")();
const inferGrp = nome => { const t = norm(nome); for(const [g, re] of GRP_REGRAS) if(re.test(t)) return g; return ""; };

const CASOS = {
  "Peito": ["Supino reto", "Supino inclinado com halteres", "Supino inclinado na máquina", "Chest press", "Bench press",
    "Crucifixo reto", "Crucifixo inclinado", "Peck deck", "Pec deck", "Voador", "Crossover na polia", "Cross over",
    "Flexão de braço", "Push up", "Dumbbell fly", "Incline dumbbell press"],
  "Costas": ["Puxada alta", "Puxada alta neutra", "Puxada frontal", "Lat pulldown", "Remada curvada", "Remada baixa",
    "Remada cavalinho", "Remada unilateral", "Serrote", "Barra fixa", "Pull up", "Chin up", "Seated row", "Pulldown"],
  "Ombro": ["Desenvolvimento com halteres", "Desenvolvimento militar", "Elevação lateral", "Elevação lateral na polia",
    "Lateral raise", "Elevação frontal", "Face pull", "Reverse fly", "Crucifixo inverso", "Voador inverso",
    "Arnold press", "Shoulder press", "Remada alta", "Rear delt fly"],
  "Bíceps": ["Rosca direta", "Rosca direta com halter", "Rosca alternada", "Rosca martelo", "Rosca scott", "Rosca concentrada",
    "Barbell curl", "Hammer curl", "Biceps curl", "Rosca na polia"],
  "Tríceps": ["Tríceps pulley", "Tríceps na polia", "Tríceps corda", "Tríceps testa", "Tríceps francês", "Pushdown",
    "Skull crusher", "Mergulho no banco", "Dip", "Extensão de tríceps"],
  "Quadríceps": ["Agachamento livre", "Agachamento no smith", "Leg press", "Leg press 45", "Hack squat", "Cadeira extensora",
    "Leg extension", "Afundo", "Passada", "Lunge", "Agachamento búlgaro", "Squat"],
  "Posterior": ["Cadeira flexora", "Mesa flexora", "Leg curl", "Stiff", "Romanian deadlift", "Levantamento terra romeno", "Good morning"],
  "Glúteo": ["Hip thrust", "Elevação pélvica", "Glúteo na polia", "Coice na polia", "Kickback", "Cadeira abdutora", "Abdutora"],
  "Panturrilha": ["Panturrilha em pé", "Panturrilha sentado", "Gêmeos no leg press", "Calf raise", "Elevação de panturrilha"],
  "Antebraço": ["Wrist curl", "Reverse wrist curl", "Reverse curl", "Rosca inversa", "Rosca punho", "Extensão de punho", "Flexão de punho", "Antebraço na polia", "Farmer walk"],
  "Abdômen": ["Abdominal", "Abdominal infra", "Abdominal na máquina", "Crunch", "Prancha", "Plank", "Elevação de pernas", "Leg raise", "Roda abdominal", "Oblíquo"]
};

let falhas = 0, total = 0;
for(const [esperado, nomes] of Object.entries(CASOS)){
  for(const nome of nomes){
    total++;
    const obtido = inferGrp(nome);
    if(obtido !== esperado){ falhas++; console.log(`FALHOU  "${nome}": esperado ${esperado}, deu ${obtido || "(sem grupo)"}`); }
  }
}
console.log(falhas ? `\n${falhas} de ${total} falharam.` : `OK — ${total} nomes no grupo certo.`);
process.exit(falhas ? 1 : 0);
