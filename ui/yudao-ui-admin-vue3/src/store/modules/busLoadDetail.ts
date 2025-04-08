import { defineStore } from 'pinia'

export interface BusLoadDetailState {
  devKey: String
  busId: String
  location: String
  busName: String
  roomName: String
}
export const useBusLoadDetailStore = defineStore('busLoadDetail', {
  state: (): BusLoadDetailState => ({
    devKey: '',
    busId: '',
    location: '',
    busName: '',
    roomName: '',
  }),
  actions: {
    updateBusLoadDetail(newDevKey: string,newBusId: string,newLocation: string,newBusName: string,newRoomName: string) {
      this.devKey = newDevKey
      this.busId = newBusId
      this.location = newLocation
      this.busName = newBusName
      this.roomName = newRoomName
    }
  }
})
