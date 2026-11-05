package com.healthcare.fonolab.domain.datetime.wasm

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("() => Date.now()")
private external fun jsCurrentTimeMillis(): Double

internal fun currentTimeMillis(): Long = jsCurrentTimeMillis().toLong()
