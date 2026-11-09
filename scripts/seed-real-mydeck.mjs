#!/usr/bin/env node
/**
 * Dados de teste em produção controlada — Firebase mydeck-kotlin.
 * SEGURO POR PADRÃO: sem --apply e confirmação exata, NÃO usa credenciais nem escreve.
 * Nunca substitui documentos ou contas que não pertençam ao seed.
 *
 * Para rodar: npm install
 *   gcloud auth application-default login
 *   node scripts/seed-real-mydeck.mjs --apply --project mydeck-kotlin --confirm REAL-mydeck-kotlin
 *
 * Guarda senhas APENAS em .mydeck-test-credentials.local.json (ignorado pelo Git).
 */
import { randomBytes } from "node:crypto";
import { existsSync, readFileSync, writeFileSync } from "node:fs";
import { resolve } from "node:path";
import { initializeApp, applicationDefault } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { getFirestore, FieldValue } from "firebase-admin/firestore";

const TARGET = "mydeck-kotlin";
const MARKER = "qa-mydeck-real-seed-v1";
const MANIFEST_ID = "qa_mydeck_real_v1";
const SECRET_FILE = resolve(".mydeck-test-credentials.local.json");

const ACCOUNTS = [
  { role: "teacher", uid: "qa_mydeck_teacher_v1", name: "Professor Teste",
    username: "professor.teste", email: "professor.teste.mydeck@example.com" },
  { role: "student", uid: "qa_mydeck_student_v1", name: "Aluno Teste",
    username: "aluno.teste", email: "aluno.teste.mydeck@example.com" }
];

const CLASSROOMS = [
  { id: "qa_mydeck_mat_v1", name: "[TESTE] Matemática", code: "QA2MATH3" },
  { id: "qa_mydeck_ingles_v1", name: "[TESTE] Inglês", code: "QA2LANG3" },
  { id: "qa_mydeck_ciencias_v1", name: "[TESTE] Ciências", code: "QA2NATU3" }
];

const DECKS = [
  { id: "qa_mydeck_deck_mat_v1", title: "[TESTE] Fundamentos de matemática",
    classes: ["qa_mydeck_mat_v1"] },
  { id: "qa_mydeck_deck_ingles_v1", title: "[TESTE] Vocabulário inglês",
    classes: ["qa_mydeck_ingles_v1"] },
  { id: "qa_mydeck_deck_ciencias_v1", title: "[TESTE] Ciências naturais",
    classes: ["qa_mydeck_ciencias_v1"] },
  { id: "qa_mydeck_deck_compartilhado_v1", title: "[TESTE] Revisão multidisciplinar",
    classes: ["qa_mydeck_mat_v1", "qa_mydeck_ingles_v1"] },
  { id: "qa_mydeck_deck_livre_v1", title: "[TESTE] Não atribuído",
    classes: [] }
];

// Cinco cartões por deck, um de cada tipo suportado pelo aplicativo.
const THEMES = [
  { basic: ["Quanto é 5 + 7?", "12"],
    choice: ["Qual é o resultado de 9 × 3?", ["18", "27", "36"], 1],
    cloze: ["Um triângulo tem ___ lados.", "três"],
    reversible: ["Eight", "Oito"],
    open: "Explique como calcular a área de um retângulo.", tag: "matemática" },
  { basic: ["Como se diz 'casa' em inglês?", "House"],
    choice: ["Qual palavra significa 'livro'?", ["Pen", "Book", "Door"], 1],
    cloze: ["Good ___ significa 'bom dia'.", "morning"],
    reversible: ["Apple", "Maçã"],
    open: "Escreva uma frase simples em inglês.", tag: "idiomas" },
  { basic: ["Qual planeta é conhecido como planeta vermelho?", "Marte"],
    choice: ["Qual é o maior órgão do corpo humano?", ["Pele", "Fígado", "Pulmão"], 0],
    cloze: ["As plantas realizam a ___.", "fotossíntese"],
    reversible: ["Water", "Água"],
    open: "Explique o ciclo da água.", tag: "ciências" },
  { basic: ["Qual é a capital do Brasil?", "Brasília"],
    choice: ["Quanto é 2²?", ["2", "4", "8"], 1],
    cloze: ["O sol nasce no ___.", "leste"],
    reversible: ["Study", "Estudar"],
    open: "O que ajuda você a aprender um conceito difícil?", tag: "revisão" },
  { basic: ["Qual é o símbolo químico da água?", "H₂O"],
    choice: ["Quantos lados tem um pentágono?", ["4", "5", "6"], 1],
    cloze: ["A Terra gira ao redor do ___.", "Sol"],
    reversible: ["Hello", "Olá"],
    open: "Descreva uma estratégia de memorização.", tag: "geral" }
];

function cardFields(deckIndex, kind) {
  const theme = THEMES[deckIndex];
  const common = {
    tags: [theme.tag],
    description: "Conteúdo fictício para testes do MyDeck.",
    options: [],
    correctOptionIndex: null,
    frontLanguage: "",
    backLanguage: "",
    mediaType: "",
    mediaUrl: "",
    createdAt: FieldValue.serverTimestamp()
  };
  switch (kind) {
    case "BASIC":
      return { ...common, type: kind, question: theme.basic[0], answer: theme.basic[1] };
    case "MULTIPLE_CHOICE":
      return {
        ...common, type: kind, question: theme.choice[0],
        answer: theme.choice[1][theme.choice[2]],
        options: theme.choice[1], correctOptionIndex: theme.choice[2]
      };
    case "CLOZE":
      return { ...common, type: kind, question: theme.cloze[0], answer: theme.cloze[1] };
    case "REVERSIBLE":
      return {
        ...common, type: kind, question: theme.reversible[0], answer: theme.reversible[1],
        frontLanguage: "Inglês", backLanguage: "Português"
      };
    case "OPEN_ENDED":
      return { ...common, type: kind, question: theme.open, answer: "" };
    default: throw new Error("Tipo de cartão inválido.");
  }
}
const KINDS = ["BASIC", "MULTIPLE_CHOICE", "CLOZE", "REVERSIBLE", "OPEN_ENDED"];

function parseArgs(argv) {
  const args = { apply: false, project: "", confirm: "", resetPasswords: false };
  for (let i = 0; i < argv.length; i++) {
    if (argv[i] === "--apply") args.apply = true;
    else if (argv[i] === "--reset-passwords") args.resetPasswords = true;
    else if (argv[i] === "--project") args.project = argv[++i] || "";
    else if (argv[i] === "--confirm") args.confirm = argv[++i] || "";
    else throw new Error("Argumento inválido: " + argv[i]);
  }
  return args;
}

function generatePassword() {
  // 32 caracteres imprevisíveis + requisitos comuns de complexidade.
  return randomBytes(24).toString("base64url") + "aA7!";
}
function loadCredentials() {
  if (!existsSync(SECRET_FILE)) return null;
  const saved = JSON.parse(readFileSync(SECRET_FILE, "utf8"));
  if (saved.projectId !== TARGET || saved.marker !== MARKER ||
      !ACCOUNTS.every(a => typeof saved.passwords?.[a.role] === "string")) {
    throw new Error("Arquivo local de credenciais não pertence a este conjunto de testes.");
  }
  return saved;
}
function saveCredentials(credentials) {
  writeFileSync(SECRET_FILE, JSON.stringify(credentials, null, 2) + "\n",
    { mode: 0o600, flag: "w" });
}
function printCredentials(credentials) {
  console.log("\n--- ACESSO NO APLICATIVO (NÃO COMPARTILHE ESTE TERMINAL) ---");
  for (const account of ACCOUNTS) {
    console.log(account.role === "teacher" ? "\nPROFESSOR" : "\nALUNO");
    console.log("E-mail:", account.email);
    console.log("Senha:", credentials.passwords[account.role]);
  }
  console.log("\nSenhas também armazenadas apenas no seu computador:", SECRET_FILE);
}
async function findUser(auth, account) {
  const user = await auth.getUser(account.uid).catch(e => {
    if (e.code === "auth/user-not-found") return null;
    throw e;
  });
  const emailUser = await auth.getUserByEmail(account.email).catch(e => {
    if (e.code === "auth/user-not-found") return null;
    throw e;
  });
  if (emailUser && emailUser.uid !== account.uid) {
    throw new Error("O e-mail " + account.email + " pertence a outra conta. Nenhuma alteração autorizada.");
  }
  if (user && (user.email !== account.email || user.customClaims?.mydeckSeed !== MARKER)) {
    throw new Error("Conta " + account.uid + " já existe sem a marca de teste. Execução interrompida.");
  }
  return user;
}

function makeEntries(db, teacherUid, studentUid) {
  const docs = [];
  function add(path, data) { docs.push({ path, ref: db.doc(path), data }); }
  for (const room of CLASSROOMS) {
    add("classes/" + room.id, {
      name: room.name, ownerUid: teacherUid, joinCode: room.code,
      createdAt: FieldValue.serverTimestamp()
    });
    add("classCodes/" + room.code, {
      classId: room.id, ownerUid: teacherUid, createdAt: FieldValue.serverTimestamp()
    });
    add("enrollments/" + room.id + "_" + studentUid, {
      classId: room.id, studentId: studentUid, username: "aluno.teste",
      joinCode: room.code, joinedAt: FieldValue.serverTimestamp()
    });
  }
  DECKS.forEach((deck, deckIndex) => {
    add("decks/" + deck.id, {
      ownerUid: teacherUid, title: deck.title,
      assignedClassIds: deck.classes,
      createdAt: FieldValue.serverTimestamp()
    });
    deck.classes.forEach(classId => add("classDeckAssignments/" + classId + "_" + deck.id, {
      ownerUid: teacherUid, classId, deckId: deck.id
    }));
    if (deck.classes.length) {
      add("deckAccess/" + deck.id + "_" + studentUid, {
        deckId: deck.id, studentId: studentUid, classId: deck.classes[0]
      });
    }
    KINDS.forEach((type, index) => {
      const cardId = "qa_card_" + String(index + 1).padStart(2, "0");
      add("decks/" + deck.id + "/cards/" + cardId, cardFields(deckIndex, type));
      if (deck.classes.length && index < 3) {
        const now = Date.now();
        const daysAgo = 1 + deckIndex * 3 + index;
        const reviewedAt = now - daysAgo * 86_400_000;
        add("reviews/qa_review_" + deckIndex + "_" + index, {
          studentId: studentUid, ownerUid: teacherUid, deckId: deck.id,
          cardId, classId: deck.classes[0],
          correct: index % 2 === 0,
          writtenResponse: null,
          tags: [THEMES[deckIndex].tag],
          reviewedAtEpochMillis: reviewedAt,
          dueAtEpochMillis: reviewedAt + (index + 1) * 86_400_000,
          intervalDays: index + 1, repetitions: index === 0 ? 1 : 2
        });
      }
    });
  });
  for (const account of ACCOUNTS) {
    add("users/" + account.uid, {
      uid: account.uid, email: account.email, name: account.name,
      username: account.username, role: account.role,
      createdAt: FieldValue.serverTimestamp()
    });
    add("usernames/" + account.username, {
      uid: account.uid, createdAt: FieldValue.serverTimestamp()
    });
  }
  return docs;
}

async function main() {
  const args = parseArgs(process.argv.slice(2));
  const cardCount = DECKS.length * KINDS.length;
  console.log("PROJETO:", TARGET, "| Contas:", ACCOUNTS.length, "| Turmas:", CLASSROOMS.length,
    "| Decks:", DECKS.length, "| Flashcards:", cardCount);
  console.log("Professor:", ACCOUNTS[0].email, "| Aluno:", ACCOUNTS[1].email);
  console.log("Códigos:", CLASSROOMS.map(c => c.name + " = " + c.code).join("; "));

  if (!args.apply) {
    console.log("\nPré-visualização apenas. Nenhuma conta/documento criado.");
    console.log("Para executar no projeto REAL use: npm run seed:real");
    return;
  }
  if (args.project !== TARGET || args.confirm !== "REAL-" + TARGET) {
    throw new Error("Projeto real não confirmado. Exige --project " + TARGET +
      " --confirm REAL-" + TARGET);
  }
  if (process.env.GOOGLE_CLOUD_PROJECT && process.env.GOOGLE_CLOUD_PROJECT !== TARGET) {
    throw new Error("GOOGLE_CLOUD_PROJECT aponta para outro projeto.");
  }
  if (process.env.FIRESTORE_EMULATOR_HOST || process.env.FIREBASE_AUTH_EMULATOR_HOST) {
    throw new Error("Variáveis de emulador detectadas; remova-as para testar o projeto real.");
  }

  const app = initializeApp({ credential: applicationDefault(), projectId: TARGET });
  const auth = getAuth(app);
  const db = getFirestore(app);
  const manifestRef = db.doc("qaSeedManifests/" + MANIFEST_ID);
  const manifest = await manifestRef.get();
  if (manifest.exists && manifest.get("marker") !== MARKER) {
    throw new Error("Manifesto já existe mas pertence a outro seed.");
  }

  const existingUsers = await Promise.all(ACCOUNTS.map(a => findUser(auth, a)));
  const stored = loadCredentials();
  if (manifest.exists && !args.resetPasswords && stored) {
    console.log("\nEste conjunto de testes já está cadastrado; nenhuma escrita repetida.");
    printCredentials(stored);
    return;
  }
  if (manifest.exists && !args.resetPasswords && !stored) {
    throw new Error("Dados de teste existentes, mas senhas locais ausentes. Use --reset-passwords.");
  }
  if (args.resetPasswords && (!manifest.exists || existingUsers.some(u => !u))) {
    throw new Error("Reset de senha exige manifesto e as duas contas de teste já existentes.");
  }

  // Pré-checagem antes de criar contas: IDs fixos jamais sobrescrevem dados alheios.
  const teacherUid = ACCOUNTS[0].uid;
  const studentUid = ACCOUNTS[1].uid;
  const entries = makeEntries(db, teacherUid, studentUid);
  if (!manifest.exists) {
    const snapshots = await db.getAll(...entries.map(e => e.ref));
    const used = snapshots.filter(s => s.exists);
    if (used.length) {
      throw new Error("Alguns documentos de teste já existem sem manifesto (" +
        used.slice(0,3).map(s => s.ref.path).join(", ") + "). Não foram sobrescritos.");
    }
  }

  const credentials = stored && !args.resetPasswords ? stored : {
    projectId: TARGET, marker: MARKER, passwords: {
      teacher: generatePassword(), student: generatePassword()
    }
  };
  if (!stored || args.resetPasswords) saveCredentials(credentials);

  for (let i = 0; i < ACCOUNTS.length; i++) {
    const account = ACCOUNTS[i];
    if (!existingUsers[i]) {
      await auth.createUser({
        uid: account.uid, email: account.email,
        displayName: account.name,
        password: credentials.passwords[account.role],
        emailVerified: true, disabled: false
      });
      await auth.setCustomUserClaims(account.uid, { mydeckSeed: MARKER });
    } else if (args.resetPasswords) {
      await auth.updateUser(account.uid, {
        password: credentials.passwords[account.role],
        emailVerified: true
      });
    }
  }
  if (!manifest.exists) {
    if (entries.length + 1 > 450) throw new Error("Seed excede limite seguro de operações.");
    const batch = db.batch();
    for (const entry of entries) batch.create(entry.ref, entry.data);
    batch.create(manifestRef, {
      marker: MARKER, projectId: TARGET,
      createdAt: FieldValue.serverTimestamp(),
      teacherUid, studentUid,
      classIds: CLASSROOMS.map(c => c.id),
      deckIds: DECKS.map(d => d.id),
      documentCount: entries.length
    });
    await batch.commit();
    console.log("\nDados criados:", entries.length,
      "documentos + 1 manifesto. Nenhum documento existente foi substituído.");
  } else {
    console.log("\nSenhas de teste alteradas. Dados de turmas/decks preservados.");
  }
  printCredentials(credentials);
}

main().catch(error => {
  console.error("\nFALHA:", error.message);
  console.error("Nenhum dado preexistente deve ser apagado. Verifique as permissões administrativas.");
  process.exitCode = 1;
});
