package com.github.andreypfau.raptorq.encoder

data class EncoderBuilder(
    var decoderMemoryRequirement: Long = 10 * 1024 * 1024,
    var maxPacketSize: Int = 1024
) {
    fun build() {
        TODO()
    }
}
