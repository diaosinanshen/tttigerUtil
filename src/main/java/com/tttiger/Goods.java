package com.tttiger;

import com.tttiger.excel.annotation.ExcelAssociate;
import com.tttiger.excel.annotation.ExcelField;
import com.tttiger.excel.annotation.Status;
import com.tttiger.util.ExcelUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 秦浩桐
 */
public class Goods {

    public static void main(String[] args) throws IOException, IllegalAccessException {
        Goods goods = new Goods();
        goods.setGoodsId("12312");
        goods.setGoodsName("商品名称");
        goods.setStatus(1);
        goods.setPrice(1.8);
        GoodsType type = new GoodsType();
        type.setTypeId("999");
        type.setTypeName("食品");
        goods.setGoodsType(type);

        Goods goods2 = new Goods();
        goods2.setGoodsId("12312");
        goods2.setGoodsName("商品名称");
        goods2.setStatus(1);
        goods2.setPrice(1.8);
        GoodsType type2 = new GoodsType();
        type2.setTypeId("999");
        type2.setTypeName("食品");
        goods2.setGoodsType(type2);

        ExcelUtil<Goods> util = new ExcelUtil<>();
        List<Goods> list = new ArrayList<>();
        list.add(goods);
        list.add(goods2);
        OutputStream out = new FileOutputStream("E:/text.xls");
        util.exportExcel("test", list, out);
    }

    private String goodsId;
    @ExcelField(value = "商品名称", sort = -2)
    private String goodsName;

    @Status(value = 1, name = "删除")
    @Status(value = 2, name = "上架")
    @ExcelField(value = "状态", status = true)
    private Integer status;

    @ExcelAssociate(value = {"typeId", "typeName"}, sort = -1)
    private GoodsType goodsType;

    @ExcelField(value = "价格",collect = true)
    private Double price;

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public GoodsType getGoodsType() {
        return goodsType;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setGoodsType(GoodsType goodsType) {
        this.goodsType = goodsType;
    }
}
