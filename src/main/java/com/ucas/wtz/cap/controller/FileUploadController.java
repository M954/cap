package com.ucas.wtz.cap.controller;

import com.alibaba.fastjson.JSON;
import com.ucas.wtz.cap.Model.PageHelper;
import com.ucas.wtz.cap.Model.Picture;
import com.ucas.wtz.cap.Model.SysUser;
import com.ucas.wtz.cap.PictureRepository;
import com.ucas.wtz.cap.SysUserRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Controller
public class FileUploadController {
    // 这里的是application.properties中配置的地址
    @Value("${uploadpic.path}")
    private String uploadPicPath;

    @Autowired
    PictureRepository pictureRepository;
    @Autowired
    SysUserRepository sysUserRepository;

    @ResponseBody
    @PostMapping("/register")//仅供管理员使用
    public String register(@RequestParam("username") String username, @RequestParam("password") String password) {
        //如果用户名存在，返回错误
        if (sysUserRepository.findByUsername(username) != null) {
            return "username exist";
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encode = encoder.encode(password);

        if (sysUserRepository.save(new SysUser(username, encode)) != null) {
            return "success";
        } else {
            return "false";
        }
    }


    @ResponseBody
    @PostMapping("/uploadImg")
    public String uploadImg(@RequestParam("label") String label, @RequestParam("provider") String provider, @RequestParam("place") String place, @RequestParam("dateTime") String dateTime, @RequestParam("description") String description, @RequestParam("published") boolean published, @RequestParam("copyright") boolean copyright, @RequestParam("imgFile") MultipartFile file) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
        String date = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
        Date existTime = null;
        DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        if (!dateTime.equals("")) existTime = format1.parse(dateTime);
        long nextRandom = new Random().nextLong();
        String id = date + '-' + nextRandom + StringUtils.cleanPath(file.getOriginalFilename());
        String shortId = date + "-short-" + nextRandom + StringUtils.cleanPath(file.getOriginalFilename());
        storePic(file, id, shortId);
        //pictureRepository.save(new Picture(id,"\\"+id,existTime,label,provider,place,copyright,published,description));
        pictureRepository.save(new Picture(id, "/" + id, existTime, label, provider, place, copyright, published, description));
        return id;
    }

    @ResponseBody
    @Transactional
    @PostMapping("/deleteImg")
    public String deleteImg(@RequestParam("id") String id) throws Exception {
        File file = new File(uploadPicPath + id);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                pictureRepository.deleteById(id);
                System.out.println("删除单个文件" + id + "成功！");
                return "success";
            } else {
                System.out.println("删除单个文件" + id + "失败！");
                return "fail";
            }
        } else {
            System.out.println("删除单个文件失败：" + id + "不存在！");
            return "not exist";
        }
    }

    @ResponseBody
    @PostMapping("/search")
    public String search(String id, String label, String provider, String place, String starttime, String endtime,@RequestParam int pageSize,@RequestParam int currentPage) throws Exception {
        List<Picture> result = new ArrayList<Picture>();
        if (id != null && !id.equals("")) {
            result.add(pictureRepository.findById(id));
        } else {
            if (label != null && !label.equals("")) {//标签查找约定，返回全部符合的图片，即图片必须符合所有标签才返回
                List<Picture> tmp = new ArrayList<Picture>();
                if (provider != null && !provider.equals("")) {
                    if (place != null && !place.equals("")) {
                        tmp = pictureRepository.findByPlaceAndProvider(place, provider);
                    } else {
                        tmp = pictureRepository.findByProvider(provider);
                    }
                } else {
                    if (place != null && !place.equals("")) {
                        tmp = pictureRepository.findByPlace(place);
                    } else {
                        tmp = pictureRepository.findAll();
                    }
                }
                String[] labels = label.split("\t");
                for (Picture pic : tmp) {
                    boolean flag = false;
                    for (int i = 0; i < labels.length; i++) {
                        if (!pic.getLabel().contains(labels[i])) {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        result.add(pic);
                    }
                }
            } else {
                if (provider != null && !provider.equals("")) {
                    if (place != null && !place.equals("")) {
                        result = pictureRepository.findByPlaceAndProvider(place, provider);
                    } else {
                        result = pictureRepository.findByProvider(provider);
                    }
                } else {
                    if (place != null && !place.equals("")) {
                        result = pictureRepository.findByPlace(place);
                    } else {
                        result = pictureRepository.findAll();
                    }
                }
            }
        }
        List<Picture> timeRe = new ArrayList<>();
        Date sT = null, eT = null;
        if (starttime != null && (!starttime.equals(""))) sT = new SimpleDateFormat("yyyy-MM-dd").parse(starttime);
        if (endtime != null && (!endtime.equals(""))) eT = new SimpleDateFormat("yyyy-MM-dd").parse(endtime);

        for (Picture pic : result) {
            if (sT != null) {
                if (pic.getDateTime() == null || pic.getDateTime().toString().equals("")) {
                    continue;
                }
                Date picDate = new SimpleDateFormat("yyyy-MM-dd").parse(pic.getDateTime().toString());
                if ((picDate.after(sT) && picDate.before(eT)) || picDate.equals(sT) || picDate.equals(eT)) {
                    timeRe.add(pic);
                }
            } else {
                timeRe.add(pic);
            }
        }
        if(pageSize<=0){
            pageSize = timeRe.size();
        }
        if(currentPage<=0){
            currentPage = 1;
        }
        PageHelper pageHelper = new PageHelper();
        pageHelper.setCurrentPage(currentPage);
        pageHelper.setPageSize(pageSize);
        pageHelper.setTotalPageNum((int) Math.ceil(timeRe.size() * 1.0 / pageSize));

        pageHelper.setData(timeRe.subList(Math.min((currentPage - 1) * pageSize,timeRe.size()-1), Math.min(currentPage * pageSize,timeRe.size())));
        String re = JSON.toJSONString(pageHelper);
        System.out.println(re);
        return re;
    }

    @Value("${thumbnails.width}")
    private int width;
    @Value("${thumbnails.height}")
    private int height;


    private boolean storePic(MultipartFile file, String filename, String shortFileName) throws Exception {
        //String filename = StringUtils.cleanPath(file.getOriginalFilename());
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, Paths.get(uploadPicPath + filename), // somewhat tricky
                    StandardCopyOption.REPLACE_EXISTING);
            Thumbnails.of(uploadPicPath + filename).size(width, height).toFile(uploadPicPath + shortFileName);
        } catch (Exception e) {
            throw new Exception("失败！" + filename, e);
        }
        return true;
    }
}
