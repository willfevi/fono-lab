package com.healthcare.fonolab.domain.files

actual typealias File = com.healthcare.fonolab.domain.files.wasm.File

// The browser exporter handles downloads. The legacy direct filesystem path is a no-op on Web.
actual fun File.writeText(text: String) = Unit
