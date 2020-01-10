package com.tttiger;

import com.tttiger.sql.BaseMapper;
import com.tttiger.sql.Executor;

/**
 * @author 秦浩桐
 * @version 1.0
 * @date 2020/01/10 15:51
 */
public class TestMapper extends BaseMapper<Goods> {
    public static void main(String[] args) {
        Goods goods = new Goods();
        goods.setGoodsId("12312");
        goods.setGoodsName("商品名称");
        goods.setStatus(1);
        goods.setPrice(1.8);
        GoodsType type = new GoodsType();
        type.setTypeId("999");
        type.setTypeName(null);
        goods.setGoodsType(type);
        TestMapper mapper = new TestMapper();
//        mapper.insert(goods);
//        mapper.selectById("1");
        mapper.updateById(goods);
//        mapper.deleteById("1");
//        QueryWrapper wrapper2 = new QueryWrapper();
//        QueryWrapper wrapper = new QueryWrapper();
//        wrapper.eq("goods_name","a").or().eq("goods_name","b");
//        wrapper.or().groupCondition(wrapper2.eq("goods_type","1").or().eq("goods_name","2"));
//        mapper.select(wrapper);
    }

    @Override
    public Executor<Goods> getExecutor() {
        return null;
    }
}
