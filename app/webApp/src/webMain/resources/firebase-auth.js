import { initializeApp, getApps } from "https://www.gstatic.com/firebasejs/11.0.2/firebase-app.js";
import { getAuth, GoogleAuthProvider, signInWithPopup, signOut as firebaseSignOut } from "https://www.gstatic.com/firebasejs/11.0.2/firebase-auth.js";
import { firebaseConfig } from "./firebase-config.js";

function auth() {
  if (firebaseConfig.apiKey === "REPLACE_ME") throw new Error("Firebase Web configuration has not been set");
  const app = getApps()[0] ?? initializeApp(firebaseConfig);
  return getAuth(app);
}

function record(user) {
  if (!user) return null;
  return { uid: user.uid, email: user.email, displayName: user.displayName, photoUrl: user.photoURL };
}

window.mhsFirebaseAuth = {
  async signInWithGoogle() { return record((await signInWithPopup(auth(), new GoogleAuthProvider())).user); },
  async signOut() { await firebaseSignOut(auth()); },
  async currentUser() { return record(auth().currentUser); },
  async getIdToken() { return auth().currentUser ? auth().currentUser.getIdToken() : null; }
};
