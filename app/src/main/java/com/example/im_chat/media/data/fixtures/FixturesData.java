package com.example.im_chat.media.data.fixtures;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.UUID;

/*
 * Created by Anton Bevza on 1/13/17.
 */
abstract class FixturesData {

    static SecureRandom rnd = new SecureRandom();

    static ArrayList<String> avatars = new ArrayList<String>() {
        {
            add("http://d.lanrentuku.com/down/png/1904/international_food/fried_rice.png");
            add("http://d.lanrentuku.com/down/png/1904/international_food/fried_rice.png");
            add("http://d.lanrentuku.com/down/png/1904/international_food/fried_rice.png");
        }
    };

    static final ArrayList<String> groupChatImages = new ArrayList<String>() {
        {
            //add("http://raw.githubusercontent.com/songjihu/gcsj/SJH/team1.png");
            add("http://d.lanrentuku.com/down/png/1904/international_food/fried_rice.png");
            add("http://d.lanrentuku.com/down/png/1904/international_food/fried_rice.png");
        }
    };

    static final ArrayList<String> groupChatTitles = new ArrayList<String>() {
        {
            add("Samuel, Michelle");
            add("Jordan, Jordan, Zoe");
            add("Julia, Angel, Kyle, Jordan");
        }
    };

    static final ArrayList<String> names = new ArrayList<String>() {
        {
            add("Samuel Reynolds");
            add("Kyle Hardman");
            add("Zoe Milton");
            add("Angel Ogden");
            add("Zoe Milton");
            add("Angelina Mackenzie");
            add("Kyle Oswald");
            add("Abigail Stevenson");
            add("Julia Goldman");
            add("Jordan Gill");
            add("Michelle Macey");
        }
    };

    static final ArrayList<String> messages = new ArrayList<String>() {
        {
            add("Hello!");
            add("This is my phone number - +1 (234) 567-89-01");
            add("Here is my e-mail - myemail@example.com");
            add("Hey! Check out this awesome link! www.github.com");
            add("Hello! No problem. I can today at 2 pm. And after we can go to the office.");
            add("At first, for some time, I was not able to answer him one word");
            add("At length one of them called out in a clear, polite, smooth dialect, not unlike in sound to the Italian");
            add("By the bye, Bob, said Hopkins");
            add("He made his passenger captain of one, with four of the men; and himself, his mate, and five more, went in the other; and they contrived their business very well, for they came up to the ship about midnight.");
            add("So saying he unbuckled his baldric with the bugle");
            add("Just then her head struck against the roof of the hall: in fact she was now more than nine feet high, and she at once took up the little golden key and hurried off to the garden door.");
        }
    };

    //按加号后刷新的照片地址
    static final ArrayList<String> images = new ArrayList<String>() {
        {
            //add("http://habrastorage.org/getpro/habr/post_images/e4b/067/b17/e4b067b17a3e414083f7420351db272b.jpg");
            //add("http://cdn.pixabay.com/photo/2017/12/25/17/48/waters-3038803_1280.jpg");
            add("http://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=2316337112,901857741&fm=26&gp=0.jpg");
            add("http://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1584444782252&di=241c0f05789405cd8e01e1297ea6e43f&imgtype=jpg&src=http%3A%2F%2Fimg0.imgtn.bdimg.com%2Fit%2Fu%3D663150610%2C2399252282%26fm%3D214%26gp%3D0.jpg");
            add("http://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1584444711321&di=c1de0bd9627d6de44dc321de3a67624c&imgtype=0&src=http%3A%2F%2Fwww.aliyuncs.cn%2Fcontent%2Fuploadfile%2F201908%2Ff1e51566523770.jpg");
            add("http://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1584444711320&di=6ca034913954ebd5d9c88c7bba0315b1&imgtype=0&src=http%3A%2F%2Fimg.mp.itc.cn%2Fupload%2F20170508%2Ff2dc6b2be595434a838c3600de184741_th.jpg");
            add("http://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=2028042787,2941183414&fm=26&gp=0.jpg");
            add("http://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1584444711319&di=d61b28aed4d6c6dc8df87e4807de4a8a&imgtype=0&src=http%3A%2F%2Fwww.seotest.cn%2Fd%2Ffile%2Fnews%2F20180209%2F201712016872_301.jpg");
            add("http://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1584444711318&di=d972360ec062b8255ba04b8ff9df14f8&imgtype=0&src=http%3A%2F%2Faliyunzixunbucket.oss-cn-beijing.aliyuncs.com%2Fjpg%2F519ec59ac06a92ba278dcb205a87df92.jpg%3Fx-oss-process%3Dimage%2Fresize%2Cp_100%2Fauto-orient%2C1%2Fquality%2Cq_90%2Fformat%2Cjpg%2Fwatermark%2Cimage_eXVuY2VzaGk%3D%2Ct_100");
        }
    };

    static String getRandomId() {
        return Long.toString(UUID.randomUUID().getLeastSignificantBits());
    }

    static String getRandomAvatar() {
        return avatars.get(rnd.nextInt(avatars.size()));
    }

    static String getRandomGroupChatImage() {
        return groupChatImages.get(rnd.nextInt(groupChatImages.size()));
    }

    static String getRandomGroupChatTitle() {
        return groupChatTitles.get(rnd.nextInt(groupChatTitles.size()));
    }

    static String getRandomName() {
        return names.get(rnd.nextInt(names.size()));
    }

    static String getRandomMessage() {
        return messages.get(rnd.nextInt(messages.size()));
    }

    static String getRandomImage() {
        return images.get(rnd.nextInt(images.size()));
    }

    static boolean getRandomBoolean() {
        return rnd.nextBoolean();
    }
}
