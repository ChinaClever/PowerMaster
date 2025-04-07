import { defineStore } from 'pinia'

export interface BoxLoadDetailState {
  devKey: String
  boxId: String
  location: String
  roomName: String
}
export const useBoxLoadDetailStore = defineStore('boxLoadDetail', {
  state: (): BoxLoadDetailState => ({
    devKey: '',
    boxId: '',
    location: '',
    roomName: '',
  }),
  actions: {
    updateBoxLoadDetail(newDevKey: string,newBoxId: string,newLocation: string,newRoomName: string) {
      this.devKey = newDevKey
      this.boxId = newBoxId
      this.location = newLocation
      this.roomName = newRoomName
    }
  }
})
