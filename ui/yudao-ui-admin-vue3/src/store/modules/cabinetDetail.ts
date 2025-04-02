import { defineStore } from 'pinia'

export interface CabinetDetailState {
  location: String
  cabinetName: String
  id: String
  roomId: String
  type: String
}

export const useCabinetDetailStore = defineStore('cabinetDetail', {
  state: (): CabinetDetailState => ({
    location: '',
    cabinetName: '',
    id: '',
    roomId: '',
    type: '',
  }),
  actions: {
    updateCabinetDetail(newLocation: string,newCabinetName: string,newId: string,newRoomId: string,newType: string) {
      this.location = newLocation
      this.cabinetName = newCabinetName
      this.id = newId
      this.roomId = newRoomId
      this.type = newType
    }
  }
})
