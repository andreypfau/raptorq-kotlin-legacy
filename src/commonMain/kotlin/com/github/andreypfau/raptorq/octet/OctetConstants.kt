@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.raptorq.octet

// Constants described in 5.7.3
val OCTET_EXP = ubyteArrayOf(
    1u, 2u, 4u, 8u, 16u, 32u, 64u, 128u, 29u, 58u, 116u, 232u, 205u, 135u, 19u, 38u, 76u,
    152u, 45u, 90u, 180u, 117u, 234u, 201u, 143u, 3u, 6u, 12u, 24u, 48u, 96u, 192u, 157u,
    39u, 78u, 156u, 37u, 74u, 148u, 53u, 106u, 212u, 181u, 119u, 238u, 193u, 159u, 35u,
    70u, 140u, 5u, 10u, 20u, 40u, 80u, 160u, 93u, 186u, 105u, 210u, 185u, 111u, 222u,
    161u, 95u, 190u, 97u, 194u, 153u, 47u, 94u, 188u, 101u, 202u, 137u, 15u, 30u, 60u,
    120u, 240u, 253u, 231u, 211u, 187u, 107u, 214u, 177u, 127u, 254u, 225u, 223u, 163u,
    91u, 182u, 113u, 226u, 217u, 175u, 67u, 134u, 17u, 34u, 68u, 136u, 13u, 26u, 52u,
    104u, 208u, 189u, 103u, 206u, 129u, 31u, 62u, 124u, 248u, 237u, 199u, 147u, 59u,
    118u, 236u, 197u, 151u, 51u, 102u, 204u, 133u, 23u, 46u, 92u, 184u, 109u, 218u,
    169u, 79u, 158u, 33u, 66u, 132u, 21u, 42u, 84u, 168u, 77u, 154u, 41u, 82u, 164u, 85u,
    170u, 73u, 146u, 57u, 114u, 228u, 213u, 183u, 115u, 230u, 209u, 191u, 99u, 198u,
    145u, 63u, 126u, 252u, 229u, 215u, 179u, 123u, 246u, 241u, 255u, 227u, 219u, 171u,
    75u, 150u, 49u, 98u, 196u, 149u, 55u, 110u, 220u, 165u, 87u, 174u, 65u, 130u, 25u,
    50u, 100u, 200u, 141u, 7u, 14u, 28u, 56u, 112u, 224u, 221u, 167u, 83u, 166u, 81u,
    162u, 89u, 178u, 121u, 242u, 249u, 239u, 195u, 155u, 43u, 86u, 172u, 69u, 138u, 9u,
    18u, 36u, 72u, 144u, 61u, 122u, 244u, 245u, 247u, 243u, 251u, 235u, 203u, 139u, 11u,
    22u, 44u, 88u, 176u, 125u, 250u, 233u, 207u, 131u, 27u, 54u, 108u, 216u, 173u, 71u,
    142u, 1u, 2u, 4u, 8u, 16u, 32u, 64u, 128u, 29u, 58u, 116u, 232u, 205u, 135u, 19u, 38u,
    76u, 152u, 45u, 90u, 180u, 117u, 234u, 201u, 143u, 3u, 6u, 12u, 24u, 48u, 96u, 192u,
    157u, 39u, 78u, 156u, 37u, 74u, 148u, 53u, 106u, 212u, 181u, 119u, 238u, 193u, 159u,
    35u, 70u, 140u, 5u, 10u, 20u, 40u, 80u, 160u, 93u, 186u, 105u, 210u, 185u, 111u,
    222u, 161u, 95u, 190u, 97u, 194u, 153u, 47u, 94u, 188u, 101u, 202u, 137u, 15u, 30u,
    60u, 120u, 240u, 253u, 231u, 211u, 187u, 107u, 214u, 177u, 127u, 254u, 225u, 223u,
    163u, 91u, 182u, 113u, 226u, 217u, 175u, 67u, 134u, 17u, 34u, 68u, 136u, 13u, 26u,
    52u, 104u, 208u, 189u, 103u, 206u, 129u, 31u, 62u, 124u, 248u, 237u, 199u, 147u,
    59u, 118u, 236u, 197u, 151u, 51u, 102u, 204u, 133u, 23u, 46u, 92u, 184u, 109u, 218u,
    169u, 79u, 158u, 33u, 66u, 132u, 21u, 42u, 84u, 168u, 77u, 154u, 41u, 82u, 164u, 85u,
    170u, 73u, 146u, 57u, 114u, 228u, 213u, 183u, 115u, 230u, 209u, 191u, 99u, 198u,
    145u, 63u, 126u, 252u, 229u, 215u, 179u, 123u, 246u, 241u, 255u, 227u, 219u, 171u,
    75u, 150u, 49u, 98u, 196u, 149u, 55u, 110u, 220u, 165u, 87u, 174u, 65u, 130u, 25u,
    50u, 100u, 200u, 141u, 7u, 14u, 28u, 56u, 112u, 224u, 221u, 167u, 83u, 166u, 81u,
    162u, 89u, 178u, 121u, 242u, 249u, 239u, 195u, 155u, 43u, 86u, 172u, 69u, 138u, 9u,
    18u, 36u, 72u, 144u, 61u, 122u, 244u, 245u, 247u, 243u, 251u, 235u, 203u, 139u, 11u,
    22u, 44u, 88u, 176u, 125u, 250u, 233u, 207u, 131u, 27u, 54u, 108u, 216u, 173u, 71u,
    142u,
)

// Constants described in 5.7.4
val OCTET_LOG = ubyteArrayOf(
    0u, 0u, 1u, 25u, 2u, 50u, 26u, 198u, 3u, 223u, 51u, 238u, 27u, 104u, 199u, 75u, 4u, 100u,
    224u, 14u, 52u, 141u, 239u, 129u, 28u, 193u, 105u, 248u, 200u, 8u, 76u, 113u, 5u,
    138u, 101u, 47u, 225u, 36u, 15u, 33u, 53u, 147u, 142u, 218u, 240u, 18u, 130u, 69u,
    29u, 181u, 194u, 125u, 106u, 39u, 249u, 185u, 201u, 154u, 9u, 120u, 77u, 228u, 114u,
    166u, 6u, 191u, 139u, 98u, 102u, 221u, 48u, 253u, 226u, 152u, 37u, 179u, 16u, 145u,
    34u, 136u, 54u, 208u, 148u, 206u, 143u, 150u, 219u, 189u, 241u, 210u, 19u, 92u,
    131u, 56u, 70u, 64u, 30u, 66u, 182u, 163u, 195u, 72u, 126u, 110u, 107u, 58u, 40u,
    84u, 250u, 133u, 186u, 61u, 202u, 94u, 155u, 159u, 10u, 21u, 121u, 43u, 78u, 212u,
    229u, 172u, 115u, 243u, 167u, 87u, 7u, 112u, 192u, 247u, 140u, 128u, 99u, 13u, 103u,
    74u, 222u, 237u, 49u, 197u, 254u, 24u, 227u, 165u, 153u, 119u, 38u, 184u, 180u,
    124u, 17u, 68u, 146u, 217u, 35u, 32u, 137u, 46u, 55u, 63u, 209u, 91u, 149u, 188u,
    207u, 205u, 144u, 135u, 151u, 178u, 220u, 252u, 190u, 97u, 242u, 86u, 211u, 171u,
    20u, 42u, 93u, 158u, 132u, 60u, 57u, 83u, 71u, 109u, 65u, 162u, 31u, 45u, 67u, 216u,
    183u, 123u, 164u, 118u, 196u, 23u, 73u, 236u, 127u, 12u, 111u, 246u, 108u, 161u,
    59u, 82u, 41u, 157u, 85u, 170u, 251u, 96u, 134u, 177u, 187u, 204u, 62u, 90u, 203u,
    89u, 95u, 176u, 156u, 169u, 160u, 81u, 11u, 245u, 22u, 235u, 122u, 117u, 44u, 215u,
    79u, 174u, 213u, 233u, 230u, 231u, 173u, 232u, 116u, 214u, 244u, 234u, 168u, 80u,
    88u, 175u
)

val OCTET_MUL = calculateOctetMulTable()
//val OCTET_MUL_HI_BITS = calculateOctetMulHiTable()
//val OCTET_MUL_LO_BITS = calculateOctetMulLoTable()

private inline fun constMul(x: Int, y: Int): UByte {
    return OCTET_EXP[OCTET_LOG[x].toInt() + OCTET_LOG[y].toInt()]
}

private inline fun calculateOctetMulTable(): Array<UByteArray> {
    val result = Array(256) { UByteArray(256) }
    for (i in 0..255) {
        for (j in 0..255) {
            result[i][j] = constMul(i, j)
        }
    }
    return result
}

//private fun calculateOctetMulHiTable(): Array<UByteArray> {
//    val result = Array(256) { UByteArray(32) }
//    for (i in 0 until 256) {
//        for (j in 0 until 16) {
//            result[i][j] = constMul(i, j shl 4)
//            result[i][j + 16] = constMul(i, j shl 4)
//        }
//    }
//    return result
//}
//
//private fun calculateOctetMulLoTable(): Array<UByteArray> {
//    val result = Array(256) { UByteArray(32) }
//    for (i in 0 until 256) {
//        for (j in 0 until 16) {
//            result[i][j] = constMul(i, j)
//            result[i][j + 16] = constMul(i, j)
//        }
//    }
//    return result
//}
