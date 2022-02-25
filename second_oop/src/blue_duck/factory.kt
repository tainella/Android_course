package blue_duck

class factory {
    fun create(color: String, age: Int) = when(color) {
            "red" -> red_duck(age)
            "blue" -> duck(age)
            "yellow" -> yellow_duck(age)
            else -> duck(age)
        }
    }