package kr.puze.cog.Data

data class CogData(
    var name: String = "",
    var number: String = "",
    var date: ArrayList<String> = ArrayList(),
    var loanMoney: Int = 0,
    var loanCount: Int = 0,
    var pay: Int = 0,
    var payCount: Int = 0,
    var payName: ArrayList<String> = ArrayList()
)

