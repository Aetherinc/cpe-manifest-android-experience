package com.wb.nextgen.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by gzcheng on 4/7/16.
 */
public class TheTakeData {
    public static class TheTakeCategory{
        public int categoryId;
        public String categoryName;
        public List<TheTakeCategory> childCategories;
        public List<TheTakeProduct> products;
    }

    public static class TheTakeProductFrame{
        public FrameImages frameImages;
        public int frameTime;
        public double frameLetterboxRatio;
    }

    public static class FrameImages{
        /*1000pxFrameLink	String	https://img.thetake.com/frame_images/c9994d204a97f30ba94b38887ebdae4068848e64b64660505dd134be44c2bf9a.jpeg
                500pxFrameLink	String	https://img.thetake.com/frame_images/c90d9a64b3ab8d0e1f54a3ca9a801dd0e284f5c40ea603429e8afca11a6ee739.jpeg
                50pxFrameLink	String	https://img.thetake.com/frame_images/bd17430b105f2286bd0c7b04754cd6aea927853d92482f7bd6fb7cf6aa09ffb7.jpeg
        fullSizeFrameLink	String	https://img.thetake.com/frame_images/1f2f2a96e774b50a8563b7d9582c9a76940eaccec42f4d747de7c3547d3870a9.jpeg
                250pxFrameLink	String	https://img.thetake.com/frame_images/d197416f8b5e04ab8b5b2fc53afe843ae09300abc5c41413d176f368f9a330ce.jpeg
                125pxFrameLink	String	https://img.thetake.com/frame_images/fe47367a66b108532eacbb1245d9b13be01b1a6f7c08fda0fd9697e52a40fd9e.jpeg*/
        @SerializedName(value="1000pxLink", alternate={"1000pxFrameLink", "1000pxKeyFrameLink", "1000pxCropLink"})//"1000pxFrameLink")
        public String image1000px;
        @SerializedName(value="500pxLink", alternate={"500pxFrameLink", "500pxKeyFrameLink", "500pxCropLink"})
        public String image500px;
        @SerializedName(value="fullSizeFrameLink")
        public String image1FullSize;
        @SerializedName(value="250pxLink", alternate={"250pxFrameLink", "250pxKeyFrameLink", "250pxCropLink"})
        public String image250px;
        @SerializedName(value="125pxLink", alternate={"125pxFrameLink", "125pxKeyFrameLink", "125pxCropLink"})
        public String image125px;
        @SerializedName(value="50pxLink", alternate={"50pxFrameLink", "50pxKeyFrameLink", "50pxCropLink"})
        public String image50px;
    }




    public static class TheTakeProduct{
        @SerializedName("cropImage")
        public FrameImages cropImage;
        @SerializedName("productImage")
        public FrameImages productImage;
        @SerializedName("keyFrameImage")
        public FrameImages keyFrameImage;
        public String characterId;
        public String actorId;
        public boolean verified;
        public float keyCropProductY;
        public float keyCropProductX;
        public String actorName;
        public String mediaId;
        public String productBrand;
        @SerializedName("unavailable")
        public int bUnavailable;
        public String characterName;
        public boolean soldOut;
        public String mediaName;
        public String purchaseLink;
        public float trendingScore;
        public long keyFrameImageTime;
        public long productId;
        public float keyFrameProductX;
        public float keyFrameProductY;
        public String productPrice;
        public String productName;

        public String getThumbnailUrl(){
            return cropImage.image500px;
        }
    }

}
