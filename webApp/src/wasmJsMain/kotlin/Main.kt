import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.healthcare.fonolab.core.di.DefaultFonoLabContainer
import com.healthcare.fonolab.core.di.LocalFonoLabContainer
import com.healthcare.fonolab.core.platform.LocalPlatformServices
import com.healthcare.fonolab.shared.App
import com.healthcare.fonolab.web.createWebPlatformServices
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val platformServices = createWebPlatformServices()
    val container = DefaultFonoLabContainer(clock = platformServices.clock)

    ComposeViewport(document.body!!) {
        CompositionLocalProvider(
            LocalPlatformServices provides platformServices,
            LocalFonoLabContainer provides container
        ) {
            App(platformServices = platformServices, container = container)
        }
    }
}
