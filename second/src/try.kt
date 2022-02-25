class tryme(b: Int, var d: Int) { //в скобках главный конструктор
    private val a = 9 //приватность описывается у каждого
    public val c = 8 //по умолчанию и так публичное, можно не писать

    var b = b //или см. на d

    fun hello(): String {
        return "hello"
    }

}