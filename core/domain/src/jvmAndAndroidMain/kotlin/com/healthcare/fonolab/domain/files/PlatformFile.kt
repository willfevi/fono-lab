package com.healthcare.fonolab.domain.files

import kotlin.io.writeText as writeUtf8

actual typealias File = java.io.File

actual fun File.writeText(text: String) = writeUtf8(text)
