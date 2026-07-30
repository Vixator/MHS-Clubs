package com.precon.mhsclubs.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.precon.mhsclubs.auth.AuthService
import com.precon.mhsclubs.auth.AuthState
import com.precon.mhsclubs.auth.SignInPendingException
import com.precon.mhsclubs.model.UserRole
import com.precon.mhsclubs.ui.FigmaPage
import com.precon.mhsclubs.ui.FigmaTan
import com.precon.mhsclubs.ui.FigmaText
import kotlinx.coroutines.launch
import mhsclubs.app.shared.generated.resources.Res
import mhsclubs.app.shared.generated.resources.figma_sign_in_asset_1
import mhsclubs.app.shared.generated.resources.figma_sign_in_asset_2
import org.jetbrains.compose.resources.painterResource

@Composable
fun LoginScreen(authService: AuthService, onSignedIn: (UserRole) -> Unit, onError: (String) -> Unit) {
    val authState by authService.authState.collectAsState(initial = AuthState.SignedOut)
    val scope = rememberCoroutineScope()
    when (val state = authState) {
        is AuthState.SignedIn -> LaunchedEffect(state) { onSignedIn(state.user.role) }
        is AuthState.Error -> LaunchedEffect(state) { onError(state.message) }
        else -> Unit
    }
    LoginContent(
        isLoading = authState is AuthState.Loading,
        errorMessage = (authState as? AuthState.Error)?.message,
        onSignInClick = {
            scope.launch { authService.signInWithGoogle().exceptionOrNull()?.takeUnless { it is SignInPendingException }?.let { onError(it.message ?: "Unable to start Google sign-in") } }
        }
    )
}

@Composable
fun LoginContent(isLoading: Boolean, errorMessage: String? = null, onSignInClick: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize().background(FigmaPage), contentAlignment = Alignment.TopCenter) {
        val horizontalPadding = if (maxWidth >= 402.dp) 65.dp else 24.dp
        Column(
            modifier = Modifier.widthIn(max = 402.dp).fillMaxWidth().padding(horizontal = horizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(82.dp))
            Image(
                painter = painterResource(Res.drawable.figma_sign_in_asset_2),
                contentDescription = null,
                modifier = Modifier.size(width = 180.dp, height = 173.dp)
            )
            Spacer(Modifier.height(18.dp))
            Text("MHS", color = Color(0xFF991B1E), fontSize = 60.sp, lineHeight = 45.sp, fontWeight = FontWeight.ExtraBold)
            Text("CLUBS", color = FigmaText, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.align(Alignment.Start).padding(start = 51.dp))
            Spacer(Modifier.height(70.dp))
            if (isLoading) CircularProgressIndicator(color = FigmaTan) else {
                androidx.compose.foundation.layout.Row(
                        Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(100.dp)).background(FigmaPage)
                            .border(1.dp, FigmaTan, RoundedCornerShape(100.dp)).clickable(onClick = onSignInClick).padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.figma_sign_in_asset_1),
                            contentDescription = null,
                            modifier = Modifier.size(width = 31.dp, height = 32.dp)
                        )
                        Text("Continue with Google", color = FigmaText, fontSize = 17.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 10.dp))
                    }
            }
            Text("MCPASD student email required", color = FigmaText, fontSize = 12.sp, modifier = Modifier.padding(top = 9.dp))
            errorMessage?.let { Text(it, color = Color(0xFFFF424C), fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 12.dp)) }
        }
    }
}
