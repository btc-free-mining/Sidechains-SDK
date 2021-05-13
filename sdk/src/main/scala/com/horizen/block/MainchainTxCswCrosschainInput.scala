package com.horizen.block

import com.horizen.cryptolibprovider.CryptoLibProvider
import com.horizen.librustsidechains.FieldElement
import com.horizen.utils.{BytesUtils, Utils, VarInt}

import scala.util.Try

case class MainchainTxCswCrosschainInput(cswInputBytes: Array[Byte],
                                         amount: Long,                                        // CAmount (int64_t)
                                         sidechainId: Array[Byte],                            // uint256
                                         nullifier: Array[Byte],                              // CFieldElement
                                         mcPubKeyHash: Array[Byte],                           // uint160
                                         scProof: Array[Byte],                                // ScProof
                                         actCertDataHashOpt: Option[Array[Byte]],             // CFieldElement
                                         ceasingCumulativeScTxCommitmentTreeRoot: Array[Byte],// CFieldElement
                                         redeemScript: Array[Byte]                            // CScript
                                        ) {

  lazy val hash: Array[Byte] = BytesUtils.reverseBytes(Utils.doubleSHA256Hash(cswInputBytes))

  def size: Int = cswInputBytes.length
}


object MainchainTxCswCrosschainInput {
  def create(cswInputBytes: Array[Byte], offset: Int): Try[MainchainTxCswCrosschainInput] = Try {
    if(offset < 0)
      throw new IllegalArgumentException("Input data corrupted.")

    var currentOffset: Int = offset

    val amount: Long = BytesUtils.getReversedLong(cswInputBytes, currentOffset)
    currentOffset += 8

    val sidechainId: Array[Byte] = BytesUtils.reverseBytes(cswInputBytes.slice(currentOffset, currentOffset + 32))
    currentOffset += 32

    val nullifierSize: VarInt = BytesUtils.getReversedVarInt(cswInputBytes, currentOffset)
    currentOffset += nullifierSize.size()
    if(nullifierSize.value() != FieldElement.FIELD_ELEMENT_LENGTH)
      throw new IllegalArgumentException(s"Input data corrupted: nullifier size ${nullifierSize.value()} " +
        s"is expected to be FieldElement size ${FieldElement.FIELD_ELEMENT_LENGTH}")
    val nullifier: Array[Byte] = BytesUtils.reverseBytes(cswInputBytes.slice(currentOffset, currentOffset + nullifierSize.value().intValue()))
    currentOffset += nullifierSize.value().intValue()

    val mcPubKeyHash: Array[Byte] = BytesUtils.reverseBytes(cswInputBytes.slice(currentOffset, currentOffset + 20))
    currentOffset += 20

    val scProofSize: VarInt = BytesUtils.getReversedVarInt(cswInputBytes, currentOffset)
    currentOffset += scProofSize.size()
    if(scProofSize.value() != CryptoLibProvider.sigProofThresholdCircuitFunctions.proofSizeLength())
      throw new IllegalArgumentException(s"Input data corrupted: scProof size ${scProofSize.value()} " +
        s"is expected to be ScProof size ${CryptoLibProvider.sigProofThresholdCircuitFunctions.proofSizeLength()}")

    val scProof: Array[Byte] = cswInputBytes.slice(currentOffset, currentOffset + scProofSize.value().intValue())
    currentOffset += scProofSize.value().intValue()

    val actCertDataHashSize: VarInt = BytesUtils.getReversedVarInt(cswInputBytes, currentOffset)
    currentOffset += actCertDataHashSize.size()

    // Note: There are two valid cases for actCertDataHash: to be null or to have a FE size
    // Null case is for ceased sidechains without active certificates.
    val actCertDataHashOpt: Option[Array[Byte]] = if(actCertDataHashSize.value() == 0) {
      None
    } else {
      if (actCertDataHashSize.value() != FieldElement.FIELD_ELEMENT_LENGTH)
        throw new IllegalArgumentException(s"Input data corrupted: actCertDataHash size ${nullifierSize.value()} " +
          s"is expected to be FieldElement size ${FieldElement.FIELD_ELEMENT_LENGTH}")

      val actCertDataHash: Array[Byte] = BytesUtils.reverseBytes(
        cswInputBytes.slice(currentOffset, currentOffset + actCertDataHashSize.value().intValue()))
      currentOffset += actCertDataHashSize.value().intValue()

      Some(actCertDataHash)
    }

    val ceasingCumulativeScTxCommitmentTreeRootSize: VarInt = BytesUtils.getReversedVarInt(cswInputBytes, currentOffset)
    currentOffset += ceasingCumulativeScTxCommitmentTreeRootSize.size()

    if(ceasingCumulativeScTxCommitmentTreeRootSize.value() != FieldElement.FIELD_ELEMENT_LENGTH)
      throw new IllegalArgumentException(s"Input data corrupted: ceasingCumulativeScTxCommitmentTreeRoot size ${nullifierSize.value()} " +
        s"is expected to be FieldElement size ${FieldElement.FIELD_ELEMENT_LENGTH}")
    val ceasingCumulativeScTxCommitmentTreeRoot: Array[Byte] = BytesUtils.reverseBytes(
      cswInputBytes.slice(currentOffset, currentOffset + ceasingCumulativeScTxCommitmentTreeRootSize.value().intValue()))
    currentOffset += ceasingCumulativeScTxCommitmentTreeRootSize.value().intValue()

    val scriptLength: VarInt = BytesUtils.getReversedVarInt(cswInputBytes, currentOffset)
    currentOffset += scriptLength.size()

    val redeemScript: Array[Byte] = cswInputBytes.slice(currentOffset, currentOffset + scriptLength.value().intValue())
    currentOffset += scriptLength.value().intValue()


    new MainchainTxCswCrosschainInput(cswInputBytes.slice(offset, currentOffset),
      amount, sidechainId, nullifier, mcPubKeyHash, scProof, actCertDataHashOpt,
      ceasingCumulativeScTxCommitmentTreeRoot, redeemScript)
  }
}