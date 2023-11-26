package cf.mindaugas.jreespringdelete;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.*;

@SpringBootApplication
public class JreeSpringDeleteApplicationWithDBAndRelations {
    public static void main(String[] args) {
        SpringApplication.run(JreeSpringDeleteApplicationNoDB.class, args);
    }
}

@RestController
@RequestMapping("/blogposts")
class BlogPostController {
    @Autowired
    private BlogPostRepository bpr;

    @GetMapping("")
    public ResponseEntity<List<BlogPost>> getAll(){
        var bps = bpr.findAll();
        // for(BlogPost bp : bps)
        //     System.out.println(bp.getComments());
        return new ResponseEntity<>(bps, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogPost> getById(@PathVariable Long id){
        var bp = bpr.findById(id).get();
        // System.out.println(bp.getComments());
        return new ResponseEntity<>(bp, HttpStatus.OK);
    }
}

@RestController
@RequestMapping("/comments")
class CommentController {
    @Autowired
    private CommentRepository cr;

    @GetMapping("/{id}")
    public ResponseEntity<Comment> getById(@PathVariable Long id){
        var comment = cr.findById(id).get();
        // System.out.println(comment.getBlogPost()); // this still produces SO!
        return new ResponseEntity<>(comment, HttpStatus.OK);
    }
}

@Repository
interface BlogPostRepository extends JpaRepository<BlogPost, Long> {}

@Repository
interface CommentRepository extends JpaRepository<Comment, Long> {}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EqualsAndHashCode(exclude = "blogPost")
class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bp_id")
    // @JsonIgnore
    @JsonSerialize(using = BlogpostSerializer.class)
    private BlogPost blogPost;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EqualsAndHashCode(exclude = "comments")
class BlogPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String text;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name="bp_id")
    // @JsonIgnore
    @JsonSerialize(using = CommentsSerializer.class)
    private Set<Comment> comments;
}


class CommentsSerializer extends StdSerializer<Set<Comment>> {
    public CommentsSerializer() {
        this(null);
    }
    public CommentsSerializer(Class<Set<Comment>> t) {
        super(t);
    }

    @Override
    public void serialize(
            Set<Comment> comments,
            JsonGenerator generator,
            SerializerProvider provider)
            throws IOException, JsonProcessingException
    {
       // Set<Long> ids = new HashSet<>();
       // for (Comment comment : comments)
       //     ids.add(comment.getId());
       // generator.writeObject(ids);

        List<Map<Long, String>> idToTitleMaps = new ArrayList<>();
        for (Comment comment : comments)
            idToTitleMaps.add(new HashMap<>(){{ put(comment.getId(), comment.getText()); }});
        generator.writeObject(idToTitleMaps);
    }
}

class BlogpostSerializer extends StdSerializer<BlogPost> {
    public BlogpostSerializer() {
        this(null);
    }
    public BlogpostSerializer(Class<BlogPost> t) {
        super(t);
    }

    @Override
    public void serialize(
            BlogPost blogPost,
            JsonGenerator jgen,
            SerializerProvider provider)
            throws IOException, JsonProcessingException
    {
        jgen.writeStartObject();
        jgen.writeNumberField("id", blogPost.getId());
        jgen.writeStringField("title", blogPost.getTitle());
        jgen.writeStringField("text", blogPost.getText());
        jgen.writeEndObject();
    }
}
