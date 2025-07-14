package com.example.newsapp.ui.main;


public class News {
    public String uniquekey;
    public String title;
    public String date;
    public String category;
    public String author_name;
    public String url;
    public String thumbnail_pic_s;
    public String thumbnail_pic_s02;
    public String thumbnail_pic_s03;
    public String is_content;

    public boolean isAI = false;
}


//public class News {
//    public String item_id;   // 新闻唯一id
//    public String title;
//    public String category;
//    public boolean isAI = false;     // 如果没有isAI字段，可以先写成false
//    public String date;
//
//    // Gson 需要无参构造方法
//    public News() {}
//
////        public News(String title, String category, boolean isAI) {
////        this.title = title;
////        this.category = category;
////        this.isAI = isAI;
////    }
//}