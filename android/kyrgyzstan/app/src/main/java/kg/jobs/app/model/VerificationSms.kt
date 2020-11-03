package kg.jobs.app.model

import com.google.firebase.auth.PhoneAuthProvider

class VerificationSms(var id: String,
                      var token: PhoneAuthProvider.ForceResendingToken,
                      var code: String = "")