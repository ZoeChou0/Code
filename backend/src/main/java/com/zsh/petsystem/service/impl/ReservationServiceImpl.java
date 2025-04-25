package com.zsh.petsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsh.petsystem.dto.ReservationDTO;
import com.zsh.petsystem.mapper.PetMapper;
import com.zsh.petsystem.mapper.ReservationMapper;
import com.zsh.petsystem.mapper.ServiceItemMapper;
import com.zsh.petsystem.mapper.UserMapper;
import com.zsh.petsystem.model.Pets;
import com.zsh.petsystem.model.Reservation;
import com.zsh.petsystem.model.ServiceItem;
import com.zsh.petsystem.model.Users;
import com.zsh.petsystem.service.UserService;
import com.zsh.petsystem.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
public class ReservationServiceImpl
        extends ServiceImpl<ReservationMapper, Reservation>
        implements ReservationService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PetMapper petMapper;

    @Autowired
    private ServiceItemMapper serviceItemMapper;

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public Reservation create(ReservationDTO dto, Long userId) {
        Reservation reservation = new Reservation();

        Users user = userService.getById(userId);
        if (user == null)
            throw new RuntimeException("用户不存在 (ID: " + userId + ")");

        Pets pet = petMapper.selectById(dto.getPetId());
        if (pet == null)
            throw new RuntimeException("宠物不存在 (ID: " + dto.getPetId() + ")");
        if (!userId.equals(pet.getUserId())) {
            throw new RuntimeException("无法预约不属于自己的宠物");
        }

        reservation.setUserId(userId);
        reservation.setPetId(dto.getPetId());

        // 获取服务项并校验状态
        ServiceItem serviceItem = serviceItemMapper.selectById(dto.getServiceId());
        if (serviceItem == null)
            throw new RuntimeException("服务项不存在 (ID: " + dto.getServiceId() + ")");

        if (!"APPROVED".equals(serviceItem.getReviewStatus())) {
            if ("PENDING".equals(serviceItem.getReviewStatus())) {
                throw new RuntimeException("该服务项正在审核中，暂无法预约");
            } else if ("REJECTED".equals(serviceItem.getReviewStatus())) {
                throw new RuntimeException("该服务项审核未通过，无法预约");
            } else {
                throw new RuntimeException("该服务项当前不可用，无法预约");
            }
        }

        Integer duration = serviceItem.getDuration();
        Long providerId = serviceItem.getProviderId();

        // 比对宠物健康信息与服务要求
        checkPetEligibility(pet, serviceItem);
        // 检查服务容量
        LocalDate reservationDate = dto.getReservationDate(); // 需要从 DTO 获取日期
        if (serviceItem.getDailyCapacity() != null) {
            long currentBookings = this.lambdaQuery()
                    .eq(Reservation::getServiceId, dto.getServiceId())
                    .eq(Reservation::getReservationDate, reservationDate)
                    .ne(Reservation::getStatus, "已取消") // 排除已取消
                    .count();
            if (currentBookings >= serviceItem.getDailyCapacity()) {
                throw new RuntimeException("该服务项在 " + reservationDate + " 的预约已满");
            }
        }

        // 设置预约信息
        reservation.setUserId(userId);
        reservation.setPetId(dto.getPetId());
        reservation.setServiceId(serviceItem.getId());
        reservation.setReservationDate(dto.getReservationDate());
        reservation.setReservationTime(dto.getReservationTime());
        reservation.setStatus("已预约");

        this.save(reservation);
        return reservation;
    }

    @Override
    public List<Reservation> getByUserId(Long userId) {
        return this.lambdaQuery()
                .eq(Reservation::getUserId, userId)
                .orderByDesc(Reservation::getCreatedAt)
                .list();
    }

    @Override
    public void cancel(Long id) {
        Reservation reservation = this.getById(id);
        if (reservation == null) {
            throw new RuntimeException("预约不存在");
        }
        reservation.setStatus("已取消");
        this.updateById(reservation);
    }

    // 检查宠物是否符合服务要求
    private void checkPetEligibility(Pets pet, ServiceItem serviceItem) {
        // 疫苗要求 (requiredVaccinations 是逗号分隔的字符串)
        if (serviceItem.getRequiredVaccinations() != null && !serviceItem.getRequiredVaccinations().isEmpty()) {
            List<String> requiredVacs = Arrays.asList(serviceItem.getRequiredVaccinations().split(","));
            String petVacs = pet.getVaccinationInfo() == null ? "" : pet.getVaccinationInfo(); // 宠物的疫苗信息
            for (String required : requiredVacs) {
                if (!petVacs.contains(required.trim())) { // 简单包含检查 (实际可能需要更复杂的逻辑判断有效期)
                    throw new IllegalArgumentException(String.format("预约失败：该服务要求宠物接种 '%s' 疫苗。", required.trim()));
                }
            }
        }
        // 绝育要求
        if (Boolean.TRUE.equals(serviceItem.getRequiresNeutered()) && !Boolean.TRUE.equals(pet.getNeutered())) {
            throw new IllegalArgumentException("预约失败：该服务要求宠物已绝育。");
        }
        // 年龄要求
        if (serviceItem.getMinAge() != null && pet.getAge() < serviceItem.getMinAge()) {
            throw new IllegalArgumentException(String.format("预约失败：该服务要求宠物年龄不小于 %d。", serviceItem.getMinAge()));
        }
        if (serviceItem.getMaxAge() != null && pet.getAge() > serviceItem.getMaxAge()) {
            throw new IllegalArgumentException(String.format("预约失败：该服务要求宠物年龄不大于 %d。", serviceItem.getMaxAge()));
        }

        // 品种限制 (假设 prohibitedBreeds 是逗号分隔字符串)
        if (serviceItem.getProhibitedBreeds() != null && !serviceItem.getProhibitedBreeds().isEmpty()) {
            List<String> prohibited = Arrays.asList(serviceItem.getProhibitedBreeds().split(","));
            if (prohibited.contains(pet.getSpecies())) { // 假设 species 存的是品种
                throw new IllegalArgumentException(String.format("预约失败：该服务不接受 '%s' 品种。", pet.getSpecies()));
            }
        }
        // 性格要求 (需要根据实际情况定义匹配逻辑)
        if (serviceItem.getTemperamentRequirements() != null && !serviceItem.getTemperamentRequirements().isEmpty()) {
            // 例如，如果服务要求 "仅限友好"，而宠物性格是 "有攻击倾向"
            if ("仅限友好".equals(serviceItem.getTemperamentRequirements()) && !"友好".equals(pet.getTemperament())) {
                throw new IllegalArgumentException("预约失败：该服务仅限性格友好的宠物。");
            }
        }

    }

}