#!/usr/bin/env node
/**
 * Operação administrativa local, não executada por cliente Android.
 * Exige ADC de operador confiável com permissão IAM apropriada.
 * Não salve nem comite arquivos de credenciais.
 */
import { initializeApp, applicationDefault } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { getFirestore, FieldValue } from "firebase-admin/firestore";

const projectId = process.env.GOOGLE_CLOUD_PROJECT;
const [mode, uid] = process.argv.slice(2);

if (projectId !== "mydeck-kotlin") {
  console.error("Defina GOOGLE_CLOUD_PROJECT=mydeck-kotlin antes de continuar.");
  process.exit(2);
}
if (mode !== "--approve" || !uid || !/^[A-Za-z0-9_-]{8,150}$/.test(uid)) {
  console.error("Uso: GOOGLE_CLOUD_PROJECT=mydeck-kotlin node scripts/approve-teacher.mjs --approve <UID>");
  process.exit(2);
}

try {
  const app = initializeApp({ credential: applicationDefault(), projectId });
  const user = await getAuth(app).getUser(uid);
  if (!user.emailVerified || user.disabled) {
    throw new Error("Usuário desativado ou e-mail ainda não verificado.");
  }
  const profileRef = getFirestore(app).collection("users").doc(uid);
  const profile = await profileRef.get();
  if (!profile.exists || profile.get("role") !== "pending_teacher") {
    throw new Error("Somente perfis pending_teacher podem ser aprovados.");
  }
  await profileRef.update({
    role: "teacher",
    approvedAt: FieldValue.serverTimestamp()
  });
  console.log("Acesso de professor aprovado no projeto autorizado.");
} catch (error) {
  console.error("A aprovação não foi realizada:", error.message);
  process.exitCode = 1;
}
