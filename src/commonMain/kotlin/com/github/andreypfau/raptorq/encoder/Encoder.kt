package com.github.andreypfau.raptorq.encoder

import com.github.andreypfau.raptorq.core.ObjectTransmissionInformation

class Encoder(
    val config: ObjectTransmissionInformation,
    val blocks: Array<SourceBlockEncoder>
)
