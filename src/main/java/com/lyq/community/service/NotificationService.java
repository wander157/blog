package com.lyq.community.service;

import com.lyq.community.dto.NotificationDTO;
import com.lyq.community.dto.PaginationDTO;
import com.lyq.community.enums.NotificationStatusEnum;
import com.lyq.community.enums.NotificationTypeEnum;
import com.lyq.community.exception.CustomizeErrorCode;
import com.lyq.community.exception.CustomizeException;
import com.lyq.community.mapper.NotificationMapper;
import com.lyq.community.model.Notification;
import com.lyq.community.model.NotificationExample;
import com.lyq.community.model.User;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class NotificationService {
    @Autowired
    private NotificationMapper notificationMapper;


    public PaginationDTO list(Long id, Integer page, Integer size) {
        PaginationDTO<NotificationDTO> paginationDTO = new PaginationDTO();

        Integer totalPage;

        NotificationExample notificationExample = new NotificationExample();
        notificationExample.createCriteria()
                .andReceiverEqualTo(id);
        Integer totalCount = (int) notificationMapper.countByExample(notificationExample);

        if (totalCount % size == 0) {
            totalPage = totalCount / size;
        } else {
            totalPage = totalCount / size + 1;
        }

        if (page < 1) {
            page = 1;
        }
        if (page > totalPage) {
            page = totalPage;
        }

        paginationDTO.setPagination(totalPage, page);

        //size*(page-1)
        Integer offset = size * (page - 1);
        NotificationExample example = new NotificationExample();
        example.createCriteria()
                .andReceiverEqualTo(id);
        example.setOrderByClause("gmt_create desc");
        List<Notification> notifications = notificationMapper.selectByExampleWithRowbounds(example, new RowBounds(offset, size));
        if (notifications.size() == 0) {
            return paginationDTO;
        }
        List<NotificationDTO> notificationDTOS = new ArrayList<>();
        for (Notification notification : notifications) {
            NotificationDTO notificationDTO = new NotificationDTO();
            BeanUtils.copyProperties(notification, notificationDTO);
            notificationDTO.setTypeName(NotificationTypeEnum.nameOfType(notification.getType().intValue()));
            notificationDTOS.add(notificationDTO);
        }


        paginationDTO.setData(notificationDTOS);
        return paginationDTO;
    }

    public Long unReadCount(Integer userId) {
        NotificationExample notificationExample = new NotificationExample();
        notificationExample.createCriteria().andReceiverEqualTo(userId.longValue()).andStatusEqualTo(NotificationStatusEnum.UNREAD.getStatus());
        return notificationMapper.countByExample(notificationExample);
    }

    public NotificationDTO read(Long id, User user) {
        Notification notification = notificationMapper.selectByPrimaryKey(id.intValue());
        if (notification == null) {
            throw new CustomizeException(CustomizeErrorCode.NOTIFICATION_NOT_FOUND);
        }
        if (!Objects.equals(notification.getReceiver(), user.getId().longValue())) {
            //我得说一下为什么用longValue 因为这俩一个Long 一个Integer啊 ,设计表的时候这个伙计疯狂重构,我也很无奈啊!
            throw new CustomizeException(CustomizeErrorCode.READ_NOTIFICATION_FAIL);
        }

        notification.setStatus(NotificationStatusEnum.READ.getStatus());
        notificationMapper.updateByPrimaryKey(notification);

        NotificationDTO notificationDTO = new NotificationDTO();
        BeanUtils.copyProperties(notification, notificationDTO);
        notificationDTO.setTypeName(NotificationTypeEnum.nameOfType(notification.getType()));
        return notificationDTO;
    }
}
