package com.saathi.accessibility

/** Conservative detection, not a guarantee of complete redaction. Never used to allow cloud upload. */
object SensitiveContent {
    private val cues = Regex(
        "(?i)(?<![\\p{L}\\p{N}])(pin|otp|password|cvv|mpin|passcode|security[ _-]*code|recovery[ _-]*code)(?![\\p{L}\\p{N}])|" +
            "पासवर्ड|पिन|ओटीपी|ओ[.]टी[.]पी|गुप्त|सुरक्षा[ ]*कोड|सीवीवी"
    )
    private val digits = Regex("(?<![\\p{L}\\p{N}])\\p{N}{4,}(?![\\p{L}\\p{N}])")
    fun isSensitive(password: Boolean, vararg values: String?): Boolean = password || values.filterNotNull().any {
        val splitIdentifier = it.replace(Regex("([a-z])([A-Z])"), "$1 $2")
        cues.containsMatchIn(splitIdentifier) || digits.containsMatchIn(it)
    }
}
