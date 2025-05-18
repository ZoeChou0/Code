import request from '@/utils/request'
import type { BackendResult } from '@/types/api';
export interface Pet {
  id: number;
  userId: number;
  name: string;
  species: 'Dog' | 'Cat';
  gender: 'Male' | 'Female';
  ageInYears: number;
  ageInMonths: number;
  weight: number;
  neutered: boolean;
  photoUrl: string;
  description: string;
  vaccinationInfo: string;
  allergies: string;
  medicalNotes: string;
  temperament: string;
  energyLevel: 'High' | 'Moderate' | 'Low';
  feedingSchedule: string;
  feedingInstructions: string;
  pottyBreakFrequency: string;
  pottyBreakInstructions: string;
  aloneTimeTolerance: string;
}

// 获取宠物列表
export function getPetList(): Promise<BackendResult<Pet[]>> {
  return request({
    url: '/pets/my',
    method: 'get'
  })
}

// 添加宠物
export function addPet(data: Omit<Pet, 'id' | 'userId'>): Promise<BackendResult<Pet>> {
  return request({
    url: '/pets/add',
    method: 'post',
    data
  })
}

// 更新宠物信息
export function updatePet(data: Pet): Promise<BackendResult<Pet>> {
  return request({
    url: '/pets/update',
    method: 'put',
    data
  })
}

// 删除宠物
export function deletePet(id: number): Promise<BackendResult<null>> {
  return request({
    url: `/pets/delete/${id}`,
    method: 'delete'
  })
}

export function getPetDetail(id: number | string): Promise<BackendResult<Pet>> {
  // 确保 id 是数字或可以转为数字的字符串
  return request<BackendResult<Pet>>({
    url: `/pets/${id}`, // 假设后端获取单个宠物的接口路径是 /pets/{id}
    method: 'get'
  });
}