package com.thanhtungle.redditclone.model.request.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostRequest {

    private Long id;
    private String subredditName;
    private String name;
    private String url;
    private String description;
}
