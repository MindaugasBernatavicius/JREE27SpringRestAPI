package cf.mindaugas.jreespringdelete;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@SpringBootApplication
public class JreeSpringDeleteApplicationWithDB {
    public static void main(String[] args) {
        SpringApplication.run(JreeSpringDeleteApplicationNoDB.class, args);
    }
}

@RestController
@RequestMapping("/items")
class ItemController {
    @Autowired
    private ItemRepository itemRepository;

    // GET all ... or FIND BY title
    // http://localhost:8080/items/1
    // http://localhost:8080/items?title=xyz
    // http://localhost:8080/items?filterBy=title,hello world&sortBy=deliveryDate,asc
    // http://localhost:8080/items?sort=
    @GetMapping({ "", "/"})
    public ResponseEntity<List<Item>> getAll(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String sort
    ){
        // if(title == null) {
        //     if (sort == null)
        //         return new ResponseEntity<>(itemRepository.findAll(), HttpStatus.OK);
        //     return new ResponseEntity<>(itemRepository.findAll(Sort.by(Sort.Direction.DESC, sort)), HttpStatus.OK);
        // }
        // if (sort == null)
        //     return new ResponseEntity<>(itemRepository.findByTitle(title), HttpStatus.OK);
        // return new ResponseEntity<>(itemRepository.findByTitleOrderByCount(title), HttpStatus.OK);

        if(title == null){
            var sortInstructions = sort.split(",");
            var sortField = sortInstructions[0];
            var sortDirection = sortInstructions[1].equalsIgnoreCase("desc")
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            var items = itemRepository.findAll(Sort.by(sortDirection, sortField));
            return new ResponseEntity<>(items, HttpStatus.OK);
        }
        return new ResponseEntity<>(itemRepository.findByTitle(title), HttpStatus.OK);
    }

    // GET by id
    @GetMapping( "/{id}")
    public ResponseEntity<Item> getByID(@PathVariable Long id){
        return itemRepository.findById(id)
            .map(item -> new ResponseEntity<>(item, HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // CREATE
    @PostMapping("")
    public ResponseEntity<Void> createItem(@RequestBody Item item){
        itemRepository.save(item);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    private void mergeItems(Item original, Item donor){
        original.setTitle(donor.getTitle());
        original.setCount(donor.getCount());
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateItem(@PathVariable Long id, @RequestBody Item item){
        Optional<Item> itemToUpdate = itemRepository.findById(id);
        return itemToUpdate
                .map(currentItem -> {
                    // currentItem.setTitle(item.getTitle());
                    // currentItem.setCount(item.getCount());
                    mergeItems(currentItem, item);
                    itemRepository.save(currentItem);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // DELETE
    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id){
        // if(itemRepository.existsById(id)){
        //     itemRepository.deleteItemById(id);
        //     return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        // }
        // return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        // If we don't care about differentiated response codes
        itemRepository.deleteItemById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

@Repository // ... adds flush and deleteInBatch capabilities
interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByTitle(String title);
    List<Item> findByTitleOrderByCount(String title);
    // List<Item> findByTitleOrderBy(String title, String orderField);

    @Modifying
    @Query(value = "DELETE FROM Item WHERE id = :id")
    void deleteItemById(Long id);
}

@RequiredArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@ToString
class Item {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NonNull private String title;
    @NonNull private Integer count;
}

@Service
class DbInit implements CommandLineRunner {
    @Autowired
    private ItemRepository itemRepository;

    @Override
    public void run(String... args) {
        itemRepository.deleteAll();

        // Create initial dummy products
        var i1 = new Item("Snowboard", 100);
        var i2 = new Item("Kittens", 755);
        var i3 = new Item("Small dogs", 855);
        var i4 = new Item("Tesla P100", 655);

        // Save to db
        itemRepository.saveAll(Arrays.asList(i1, i2, i3, i4));
    }

}