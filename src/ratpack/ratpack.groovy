import static ratpack.groovy.Groovy.ratpack
import static ratpack.jackson.Jackson.json

ratpack {
    handlers {
        get {
            render json([rating:"Hello, World"])
        }
    }
}