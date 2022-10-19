@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.raptorq.octet

/*

// An octet vec containing only binary values, which are bit-packed for efficiency
pub struct BinaryOctetVec {
    // Values are stored packed into the highest bits, with the last value at the highest bit of the
    // last byte. Therefore, there may be trailing bits (least significant) which are unused
    elements: Vec<u64>,
    length: usize,
}

impl BinaryOctetVec {
    pub(crate) const WORD_WIDTH: usize = 64;

    pub fn new(elements: Vec<u64>, length: usize) -> Self {
        assert_eq!(
            elements.len(),
            (length + Self::WORD_WIDTH - 1) / Self::WORD_WIDTH
        );
        BinaryOctetVec { elements, length }
    }

    pub fn len(&self) -> usize {
        self.length
    }

    fn to_octet_vec(&self) -> Vec<u8> {
        let mut word = 0;
        let mut bit = self.padding_bits();

        let result = (0..self.length)
            .map(|_| {
                let value = if self.elements[word] & BinaryOctetVec::select_mask(bit) == 0 {
                    0
                } else {
                    1
                };

                bit += 1;
                if bit == 64 {
                    word += 1;
                    bit = 0;
                }

                value
            })
            .collect();
        assert_eq!(word, self.elements.len());
        assert_eq!(bit, 0);
        result
    }

    pub fn padding_bits(&self) -> usize {
        (BinaryOctetVec::WORD_WIDTH - (self.length % BinaryOctetVec::WORD_WIDTH))
            % BinaryOctetVec::WORD_WIDTH
    }

    pub fn select_mask(bit: usize) -> u64 {
        1u64 << (bit as u64)
    }
}
 */
class BinaryOctetVec(
    val elements: ULongArray,
    val length: Int
) {
    companion object {
        const val WORD_WIDTH = 64

        fun selectMask(bit: Int): ULong = 1uL shl bit
    }

    fun paddingBits(): Int {
        return (WORD_WIDTH - (length % WORD_WIDTH)) % WORD_WIDTH
    }

    fun toOctetVec(): ByteArray {
        var word = 0
        var bit = paddingBits()

        val result = ByteArray(length) {
            val value = if (elements[word] and selectMask(bit) == 0uL) 0 else 1
            bit += 1
            if (bit == 64) {
                word += 1
                bit = 0
            }
            value.toByte()
        }
        check(word == elements.size)
        check(bit == 0)
        return result
    }
}
