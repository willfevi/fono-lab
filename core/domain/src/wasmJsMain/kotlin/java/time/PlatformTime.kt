package java.time

@JsFun("() => Date.now()")
private external fun jsCurrentTimeMillis(): Double

actual fun currentTimeMillis(): Long = jsCurrentTimeMillis().toLong()
