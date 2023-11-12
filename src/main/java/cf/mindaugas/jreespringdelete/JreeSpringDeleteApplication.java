package cf.mindaugas.jreespringdelete;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@Controller
public class JreeSpringDeleteApplication {
    public static void main(String[] args) {
        var ret = SpringApplication.run(JreeSpringDeleteApplication.class, args);
        // System.out.println(ret.getBean());
    }
}

// @Controller // MVC Controller
@RestController // for REST APIs - implies that all methods @...Mapping will return a body
@RequestMapping("/products") // prefix for all controller url mappings
class ProductController {

    // Fake database
    private List<Product> products = new ArrayList<>()
    {{
        add(new Product(1, "Snowboard", 100, 9.99, 3.75));
        add(new Product(2, "Kittens", 999, 19.99, 4.85));
        add(new Product(3, "Small dogs", 999, 19.99, 4.85));
        add(new Product(4, "Tesla P100D", 999, 19.99, 4.85));
    }};

    @GetMapping(value={"", "/"}) // GET all products
    public List<Product> getAllProducts(){
        return products;
    }

    @GetMapping("/{id}") // GET product by id
    public ResponseEntity<Product> getProductById(@PathVariable Integer id){
        var optionalProduct = products.stream().filter(p -> p.getId().equals(id)).findFirst();

        // return optionalProduct.isPresent()
        //         ? new ResponseEntity<>(optionalProduct.get(), HttpStatus.OK) // 200 OK
        //         : new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 NOT FOUND

        return optionalProduct
                .map(product -> new ResponseEntity<>(product, HttpStatus.OK)) // 200 OK
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND)); // 404 NOT FOUND
    }

    @DeleteMapping("/{id}") // DELETE product
    public void deleteProductById(@PathVariable Integer id){
        var product = getProductById(id);
        products.remove(product);
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class Product {
    private Integer id;
    private String title;
    private Integer count;
    private Double price;
    private Double rating;
}

@Controller
class HelloController {
    @RequestMapping("/")
    public @ResponseBody String root(){
        return "Hello world!";
    }

    @RequestMapping("/hello")
    public @ResponseBody String hello(){
        return "Hello world!";
    }

    @RequestMapping("/goodbye")
    public @ResponseBody String goodbye(){
        return "Goodbye world! ... " + (5 + 4);
    }
}

@Component
class CommandLineApp implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        checkFreeMemory();
    }

    private void checkFreeMemory(){
        var requiredFreeMemory = 5_000_000;
        var freeMemory = Runtime.getRuntime().freeMemory();
        if(freeMemory < requiredFreeMemory){
            System.out.println("Insufficient memory: " + freeMemory + " < " + requiredFreeMemory);
            System.exit(0);
        }
    }
}