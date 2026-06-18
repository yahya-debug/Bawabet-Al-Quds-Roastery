package app.db_proj.model;

import java.time.LocalDateTime;

public class Review {
    public final int reviewId;
    public final int personId;
    public final String personName;
    public final int itemId;
    public final String itemName;
    public final int rating;
    public final String comment;
    public final LocalDateTime reviewDate;

    public Review(int reviewId, int personId, String personName,
                  int itemId, String itemName, int rating,
                  String comment, LocalDateTime reviewDate) {
        this.reviewId = reviewId;
        this.personId = personId;
        this.personName = personName;
        this.itemId = itemId;
        this.itemName = itemName;
        this.rating = rating;
        this.comment = comment;
        this.reviewDate = reviewDate;
    }
}
