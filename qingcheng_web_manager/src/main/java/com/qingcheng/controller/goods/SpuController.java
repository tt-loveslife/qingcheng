package com.qingcheng.controller.goods;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.entity.PageResult;
import com.qingcheng.entity.Result;
import com.qingcheng.pojo.goods.Goods;
import com.qingcheng.pojo.goods.Spu;
import com.qingcheng.service.goods.SpuService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/spu")
public class SpuController {

    @Reference
    private SpuService spuService;

    @GetMapping("/findAll")
    public List<Spu> findAll(){
        return spuService.findAll();
    }

    @GetMapping("/findPage")
    public PageResult<Spu> findPage(int page, int size){
        return spuService.findPage(page, size);
    }

    @PostMapping("/findList")
    public List<Spu> findList(@RequestBody Map<String,Object> searchMap){
        return spuService.findList(searchMap);
    }

    @PostMapping("/findPage")
    public PageResult<Spu> findPage(@RequestBody Map<String,Object> searchMap,int page, int size){
        return  spuService.findPage(searchMap,page,size);
    }

    @GetMapping("/findById")
    public Spu findById(String id){
        return spuService.findById(id);
    }

    @GetMapping("/findGoodsById")
    public Goods findGoodsById(String id){
        return spuService.findGoodsById(id);
    }


    @PostMapping("/add")
    public Result add(@RequestBody Spu spu){
        spuService.add(spu);
        return new Result();
    }

    @PostMapping("/save")
    public Result add(@RequestBody Goods goods){
        spuService.saveGoods(goods);
        return new Result();
    }

    @PostMapping("/update")
    public Result update(@RequestBody Spu spu){
        spuService.update(spu);
        return new Result();
    }

    @GetMapping("/delete")
    public Result delete(String id){
        spuService.delete(id);
        return new Result();
    }

    @GetMapping("/audit")
    public Result audit(String id, String status, String message){
        spuService.audit(id, status, message);
        return new Result();
    }

    @GetMapping("/pull")
    public Result pull(String id){
        spuService.pull(id);
        return new Result();
    }
    @GetMapping("/put")
    public Result put(String id){
        spuService.put(id);
        return new Result();
    }

    @GetMapping("/putMany")
    public Result putMany(Long[] ids){
        spuService.putMany(ids);
        return new Result();
    }

    @GetMapping("/deleteGoods")
    public Result deleteGoods(String id){
        spuService.deleteGoods(id);
        return new Result();
    }

    @GetMapping("/recoverGoods")
    public Result recoverGoods(String id){
        spuService.recoverGoods(id);
        return new Result();
    }

}
