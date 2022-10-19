package com.github.andreypfau.raptorq.parameters

internal const val Al = 1

internal const val K_max = 56403 // "maximum number of symbols in each source block"
internal const val Z_max = 256 // "maximum number of source blocks"
internal const val Kt_max = K_max * Z_max // "maximum number of symbols"
internal const val T_max = 65535 / Al * Al // "maximum symbol size, in octets"
internal const val F_max = Kt_max.toLong() * T_max // "maximum transfer length of the object, in octets"
internal const val N_max = 1 // "maximum interleaver length, in number of sub-blocks"

internal const val K_min = 1
internal const val K_prime_min = 10 // the first K' value in the systematic indices table

internal const val Z_min = 1
internal const val T_min = Al
internal const val F_min = 1L // RFC 6330 defines F as a non-negative value, but we force a positive value here

internal const val N_min = 1

internal const val SBN_max = 255
internal const val ESI_max = 16777215

internal const val SBN_min = 0
internal const val ESI_min = 0

internal const val F_num_bytes = 5
internal const val ESI_num_bytes = 3

internal const val common_OTI_reserved_inverse_mask = -0xff0001L // third octet is reserved bits
