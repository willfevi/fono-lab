package com.healthcare.fonolab.domain.files

expect open class File {
    constructor(pathname: String)
    constructor(parent: File?, child: String)

    fun mkdirs(): Boolean
    fun getName(): String
}

expect fun File.writeText(text: String)
