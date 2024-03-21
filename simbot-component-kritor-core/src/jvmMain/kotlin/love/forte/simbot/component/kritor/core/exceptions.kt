package love.forte.simbot.component.kritor.core

import io.kritor.AuthCode

/**
 * 鉴权失败错误。
 */
@Suppress("CanBeParameter", "MemberVisibilityCanBePrivate")
public class AuthException(public val authCode: AuthCode) : IllegalStateException(authCode.toString())
