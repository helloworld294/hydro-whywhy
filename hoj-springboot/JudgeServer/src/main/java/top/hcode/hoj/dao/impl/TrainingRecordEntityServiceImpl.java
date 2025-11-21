package top.hcode.hoj.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.TrainingRecordEntityService;
import top.hcode.hoj.mapper.TrainingRecordMapper;
import top.hcode.hoj.pojo.entity.training.TrainingRecord;

@Service
public class TrainingRecordEntityServiceImpl extends ServiceImpl<TrainingRecordMapper, TrainingRecord>
        implements TrainingRecordEntityService {
}

