import org.funktionale.composition.*
import org.funktionale.partials.*
import org.funktionale.currying.*


object Funktionale {

  //val add5 = { (i:Int) -> i + 5 }
  fun main( args : Array<String> ) {
    // function compose 
    val add = { a:Int -> a + 5 }
    val multiply = { a:Int -> a*3 }
    val add_multiply = add forwardCompose multiply
    println( add_multiply(2) ) // (2 + 5) * 3

    val multiply_add = add compose multiply
    println( multiply_add(2) ) // 2*3 + 5

    // partial operation
    val addp = { a:Int,b:Int,c:Int -> a*b*c }
    val build_addp = addp.partially2(3)
    println( build_addp.partially1(5)(2) )

    //　カリー化  
    val sumint = {a:Int, b:Int, c:Int -> a+b+c}
    val curry:(Int)->(Int)->(Int)->Int = sumint.curried()
    println("curried result ${curry(1)(2)(3)}")
  }
}
