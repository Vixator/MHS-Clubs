import SwiftUI
import Shared
#if canImport(FirebaseCore)
import FirebaseCore
import FirebaseAuth
import GoogleSignIn
#endif

@main
struct iOSApp: App {
    init() {
        #if canImport(FirebaseCore)
        FirebaseApp.configure()
        IosFirebaseCoordinator.shared.start()
        #endif
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

#if canImport(FirebaseCore)
final class IosFirebaseCoordinator: NSObject {
    static let shared = IosFirebaseCoordinator()

    func start() {
        NotificationCenter.default.addObserver(self, selector: #selector(startGoogleSignIn), name: .init("MHSClubsStartGoogleSignIn"), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(signOut), name: .init("MHSClubsSignOut"), object: nil)
    }

    @objc private func startGoogleSignIn() {
        guard let presenter = UIApplication.shared.connectedScenes.compactMap({ ($0 as? UIWindowScene)?.keyWindow?.rootViewController }).first else {
            IosFirebaseAuthBridge.shared.failed(message: "Unable to present Google sign-in")
            return
        }
        GIDSignIn.sharedInstance.signIn(withPresenting: presenter) { result, error in
            if let error {
                IosFirebaseAuthBridge.shared.failed(message: error.localizedDescription)
                return
            }
            guard let user = result?.user, let idToken = user.idToken?.tokenString else {
                IosFirebaseAuthBridge.shared.failed(message: "Google did not provide an ID token")
                return
            }
            let credential = GoogleAuthProvider.credential(withIDToken: idToken, accessToken: user.accessToken.tokenString)
            Auth.auth().signIn(with: credential) { authResult, error in
                if let error { IosFirebaseAuthBridge.shared.failed(message: error.localizedDescription); return }
                guard let firebaseUser = authResult?.user else { IosFirebaseAuthBridge.shared.failed(message: "Firebase sign-in failed"); return }
                firebaseUser.getIDToken { token, error in
                    if let error { IosFirebaseAuthBridge.shared.failed(message: error.localizedDescription); return }
                    guard let email = firebaseUser.email, let token else { IosFirebaseAuthBridge.shared.failed(message: "Firebase did not return a verified identity"); return }
                    IosFirebaseAuthBridge.shared.completeSignIn(uid: firebaseUser.uid, email: email, displayName: firebaseUser.displayName, avatarUrl: firebaseUser.photoURL?.absoluteString, idToken: token)
                }
            }
        }
    }

    @objc private func signOut() { try? Auth.auth().signOut(); GIDSignIn.sharedInstance.signOut(); IosFirebaseAuthBridge.shared.signedOut() }
}
#endif
