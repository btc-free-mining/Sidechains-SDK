package com.horizen.helper

import com.google.inject.{Inject, Provider}
import com.horizen.SidechainApp
import com.horizen.secret.Secret

class SecretSubmitHelperImpl @Inject()(val appProvider: Provider[SidechainApp]) extends SecretSubmitHelper {

  @throws(classOf[IllegalArgumentException])
  override def submitSecret(secret: Secret): Unit = {
    appProvider.get().getSecretSubmitProvider.submitSecret(secret)
  }
}
