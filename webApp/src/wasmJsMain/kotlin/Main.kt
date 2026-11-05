import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.healthcare.fonolab.shared.App
import com.healthcare.fonolab.web.createWebPlatformServices
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val platformServices = createWebPlatformServices()

    ComposeViewport(document.body!!) {
        App(platformServices = platformServices)
    }
}
