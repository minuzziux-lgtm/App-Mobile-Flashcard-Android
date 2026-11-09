import { readFileSync } from "node:fs";
import { after, before, beforeEach, test } from "node:test";
import {
  assertFails, assertSucceeds, initializeTestEnvironment
} from "@firebase/rules-unit-testing";
import { doc, getDoc, getDocs, setDoc, query, where, collection, writeBatch } from "firebase/firestore";

let env;

before(async () => {
  env = await initializeTestEnvironment({
    projectId: "demo-mydeck-kotlin",
    firestore: { rules: readFileSync("firestore.rules", "utf8") }
  });
});

beforeEach(async () => {
  await env.clearFirestore();
  await env.withSecurityRulesDisabled(async context => {
    const db = context.firestore();
    await Promise.all([
      setDoc(doc(db, "users/teacher-a"), { role: "teacher", uid: "teacher-a" }),
      setDoc(doc(db, "users/teacher-b"), { role: "teacher", uid: "teacher-b" }),
      setDoc(doc(db, "users/student-a"), { role: "student", uid: "student-a", username: "aluno-a" }),
      setDoc(doc(db, "users/student-b"), { role: "student", uid: "student-b", username: "aluno-b" }),
      setDoc(doc(db, "classes/math"), { ownerUid: "teacher-a", name: "Matemática", joinCode: "ABCDEFGH" }),
      setDoc(doc(db, "classes/english"), { ownerUid: "teacher-b", name: "Inglês", joinCode: "JKLMNPQR" }),
      setDoc(doc(db, "enrollments/math_student-a"), { studentId: "student-a", classId: "math", username: "aluno-a", joinCode: "ABCDEFGH" }),
      setDoc(doc(db, "decks/deck-a"), { ownerUid: "teacher-a", title: "Frações", assignedClassIds: ["math"] }),
      setDoc(doc(db, "classDeckAssignments/math_deck-a"), { ownerUid: "teacher-a", classId: "math", deckId: "deck-a" }),
      setDoc(doc(db, "deckAccess/deck-a_student-a"), { studentId: "student-a", classId: "math", deckId: "deck-a" }),
      setDoc(doc(db, "decks/deck-a/cards/card-a"), {
        type: "BASIC", question: "Pergunta", answer: "Resposta", tags: []
      })
    ]);
  });
});

after(async () => {
  if (env) await env.cleanup();
});

test("anonymous or unrelated student cannot read another class", async () => {
  const guest = env.unauthenticatedContext().firestore();
  const outsider = env.authenticatedContext("student-b").firestore();
  await assertFails(getDoc(doc(guest, "classes/math")));
  await assertFails(getDoc(doc(outsider, "classes/math")));
});

test("enrolled student sees their class, but not unrelated cards", async () => {
  const student = env.authenticatedContext("student-a").firestore();
  await assertSucceeds(getDoc(doc(student, "classes/math")));
  await assertSucceeds(getDoc(doc(student, "decks/deck-a/cards/card-a")));
  await assertFails(getDoc(doc(student, "classes/english")));
});

test("student cannot promote themselves to teacher", async () => {
  const student = env.authenticatedContext("student-a", {email:"student-a@example.com"}).firestore();
  await assertFails(setDoc(doc(student, "users/student-a"), { role: "teacher" }));
});

test("teacher cannot create deck in someone else's class", async () => {
  const teacher = env.authenticatedContext("teacher-a").firestore();
  await assertFails(setDoc(doc(teacher, "decks/foreign"), {
    ownerUid: "teacher-a", classId: "english", title: "Não autorizado", createdAt: new Date()
  }));
});

test("outsider cannot write a review for someone else", async () => {
  const outsider = env.authenticatedContext("student-b").firestore();
  await assertFails(setDoc(doc(outsider, "reviews/not-own"), {
    studentId: "student-a", deckId: "deck-a", cardId: "card-a", correct: true, reviewedAt: new Date()
  }));
});

test("teacher can create a basic deck in own class under existing rules", async () => {
  const teacher = env.authenticatedContext("teacher-a").firestore();
  await assertSucceeds(setDoc(doc(teacher, "decks/second"), {
    ownerUid: "teacher-a", title: "Novo", assignedClassIds: [], createdAt: new Date()
  }));
});

test("student without a grant cannot read the master deck even if authenticated", async () => {
  const outsider = env.authenticatedContext("student-b").firestore();
  await assertFails(getDoc(doc(outsider, "decks/deck-a")));
});

test("student can discover assignments for enrolled class", async () => {
  const enrolled = env.authenticatedContext("student-a").firestore();
  await assertSucceeds(getDoc(doc(enrolled, "classDeckAssignments/math_deck-a")));
});

test("student cannot claim deck outside enrolled class", async () => {
  const outsider = env.authenticatedContext("student-b").firestore();
  await assertFails(setDoc(doc(outsider, "deckAccess/deck-a_student-b"), {
    studentId: "student-b", deckId: "deck-a", classId: "math"
  }));
});

test("student access is revoked when teacher removes class assignment", async () => {
  const student = env.authenticatedContext("student-a").firestore();
  await assertSucceeds(getDoc(doc(student, "decks/deck-a")));
  await env.withSecurityRulesDisabled(async c => {
    const db = c.firestore();
    const { deleteDoc } = await import("firebase/firestore");
    await deleteDoc(doc(db, "classDeckAssignments/math_deck-a"));
  });
  await assertFails(getDoc(doc(student, "decks/deck-a")));
});

test("teacher cannot attach another teacher's class to a deck", async () => {
  const teacher = env.authenticatedContext("teacher-a").firestore();
  await assertFails(setDoc(doc(teacher, "classDeckAssignments/english_deck-a"), {
    ownerUid: "teacher-a", classId: "english", deckId: "deck-a"
  }));
});

test("teacher can atomically create reusable deck and initial assignment", async () => {
  const { writeBatch } = await import("firebase/firestore");
  const teacher = env.authenticatedContext("teacher-a").firestore();
  const batch = writeBatch(teacher);
  batch.set(doc(teacher, "decks/teacher-library"), {
    ownerUid: "teacher-a", title: "Compartilhado", assignedClassIds: ["math"],
    createdAt: new Date()
  });
  batch.set(doc(teacher, "classDeckAssignments/math_teacher-library"), {
    ownerUid: "teacher-a", classId: "math", deckId: "teacher-library"
  });
  await assertSucceeds(batch.commit());
});

test("student can establish their own verified access grant", async () => {
  const student = env.authenticatedContext("student-a").firestore();
  await assertSucceeds(setDoc(doc(student, "deckAccess/deck-a_student-a"), {
    studentId: "student-a", classId: "math", deckId: "deck-a"
  }));
  await assertSucceeds(getDoc(doc(student, "decks/deck-a")));
});

test("student with a valid class code can enroll using their profile username", async () => {
  await env.withSecurityRulesDisabled(async c => {
    await setDoc(doc(c.firestore(), "classCodes/JKLMNPQR"), {
      classId: "english", ownerUid: "teacher-b"
    });
  });
  const student = env.authenticatedContext("student-a").firestore();
  await assertSucceeds(setDoc(doc(student, "enrollments/english_student-a"), {
    classId: "english", studentId: "student-a", username: "aluno-a",
    joinCode: "JKLMNPQR", joinedAt: new Date()
  }));
  await assertSucceeds(getDoc(doc(student, "classes/english")));
});

test("student cannot use a forged username when joining a class", async () => {
  await env.withSecurityRulesDisabled(async c => {
    await setDoc(doc(c.firestore(), "classCodes/JKLMNPQR"), {
      classId: "english", ownerUid: "teacher-b"
    });
  });
  const student = env.authenticatedContext("student-b").firestore();
  await assertFails(setDoc(doc(student, "enrollments/english_student-b"), {
    classId: "english", studentId: "student-b", username: "aluno-a",
    joinCode: "JKLMNPQR", joinedAt: new Date()
  }));
});

test("student can create one idempotent review and update only their own review", async () => {
  const student = env.authenticatedContext("student-a").firestore();
  const reviewRef = doc(student, "reviews/session-card");
  const payload = {
    studentId: "student-a", ownerUid: "teacher-a", deckId: "deck-a",
    cardId: "card-a", classId: "math", correct: false,
    writtenResponse: null, tags: [], reviewedAtEpochMillis: 1000,
    dueAtEpochMillis: 1000, intervalDays: 0, repetitions: 0
  };
  await assertSucceeds(setDoc(reviewRef, payload));
  await assertSucceeds(setDoc(reviewRef, {...payload, correct: true}));
  const other = env.authenticatedContext("student-b").firestore();
  await assertFails(setDoc(doc(other, "reviews/session-card"), payload));
});

test("teacher can read own student review but a foreign teacher cannot", async () => {
  await env.withSecurityRulesDisabled(async c => {
    await setDoc(doc(c.firestore(), "reviews/review-math"), {
      studentId: "student-a", ownerUid: "teacher-a", deckId: "deck-a",
      cardId: "card-a", classId: "math", correct: true, tags: [],
      writtenResponse: null, reviewedAtEpochMillis: 1000,
      dueAtEpochMillis: 2000, intervalDays: 1, repetitions: 1
    });
  });
  await assertSucceeds(getDoc(doc(env.authenticatedContext("teacher-a").firestore(), "reviews/review-math")));
  await assertFails(getDoc(doc(env.authenticatedContext("teacher-b").firestore(), "reviews/review-math")));
});

test("teacher can batch-create class and reserve its code", async () => {
  const teacher = env.authenticatedContext("teacher-a").firestore();
  const batch = writeBatch(teacher);
  batch.set(doc(teacher, "classes/new-class"), {
    ownerUid: "teacher-a", name: "Nova turma", joinCode: "WXYZ2345", createdAt: new Date()
  });
  batch.set(doc(teacher, "classCodes/WXYZ2345"), {
    classId: "new-class", ownerUid: "teacher-a", createdAt: new Date()
  });
  await assertSucceeds(batch.commit());
});

test("teacher can query only their own classes and deck library", async () => {
  const teacher = env.authenticatedContext("teacher-a").firestore();
  await assertSucceeds(getDocs(query(collection(teacher, "classes"), where("ownerUid", "==", "teacher-a"))));
  await assertSucceeds(getDocs(query(collection(teacher, "decks"), where("ownerUid", "==", "teacher-a"))));
  await assertSucceeds(getDocs(collection(teacher, "decks/deck-a/cards")));
  await assertFails(getDocs(query(collection(teacher, "classes"), where("ownerUid", "==", "teacher-b"))));
});

test("student can query only enrolled classes, assignments, and cards", async () => {
  const student = env.authenticatedContext("student-a").firestore();
  await assertSucceeds(getDocs(query(collection(student, "enrollments"), where("studentId", "==", "student-a"))));
  await assertSucceeds(getDocs(query(collection(student, "classDeckAssignments"), where("classId", "==", "math"))));
  await assertSucceeds(getDocs(collection(student, "decks/deck-a/cards")));
  await assertFails(getDocs(query(collection(student, "classDeckAssignments"), where("classId", "==", "english"))));
});

test("teacher can query own reviews while other teachers cannot", async () => {
  await env.withSecurityRulesDisabled(async c => {
    await setDoc(doc(c.firestore(), "reviews/review-for-teacher"), {
      studentId: "student-a", ownerUid: "teacher-a", deckId: "deck-a",
      cardId: "card-a", classId: "math", correct: true, tags: [],
      writtenResponse: null, reviewedAtEpochMillis: 1000,
      dueAtEpochMillis: 2000, intervalDays: 1, repetitions: 1
    });
  });
  const teacher = env.authenticatedContext("teacher-a").firestore();
  const outsider = env.authenticatedContext("teacher-b").firestore();
  await assertSucceeds(getDocs(query(collection(teacher, "reviews"), where("ownerUid", "==", "teacher-a"))));
  await assertFails(getDocs(query(collection(outsider, "reviews"), where("ownerUid", "==", "teacher-a"))));
});

test("review with an unrelated classId cannot be written", async () => {
  const student = env.authenticatedContext("student-a").firestore();
  await assertFails(setDoc(doc(student, "reviews/forged-class"), {
    studentId: "student-a", ownerUid: "teacher-a",
    deckId: "deck-a", cardId: "card-a", classId: "english",
    correct: true, writtenResponse: null, tags: [],
    reviewedAtEpochMillis: 1000, dueAtEpochMillis: 1000,
    intervalDays: 0, repetitions: 0
  }));
});
