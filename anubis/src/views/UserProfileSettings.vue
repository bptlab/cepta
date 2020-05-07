<template>
  <div class="user-profile-container">
    <edit-user-form
      :initial-user="user"
      v-on:remove="removeUser"
      v-on:update="updateUser"
      :show-delete-button="true"
      :show-update-button="true"
    ></edit-user-form>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import { UserManagementModule } from "../store/modules/usermgmt";
import {
  RemoveUserRequest,
  UpdateUserRequest
} from "../generated/protobuf/models/grpc/usermgmt_pb";
import { User } from "../generated/protobuf/models/internal/types/users_pb";
import EditUserForm from "../components/EditUserForm.vue";
import { AuthModule } from "../store/modules/auth";

@Component({
  name: "UserProfileSettings",
  components: { EditUserForm }
})
export default class UserProfileSettings extends Vue {
  protected user: User | null = null;

  removeUser(req: RemoveUserRequest) {
    UserManagementModule.removeUser(req)
      .then(() => {
        AuthModule.authLogout();
      })
      .catch(err => {
        alert(`Failed to remove account: ${err.message}`);
      });
  }

  updateUser(req: UpdateUserRequest) {
    UserManagementModule.updateUser(req).catch(err => {
      alert(`Failed to update user profile: ${err.message}`);
    });
  }

  mounted() {
    UserManagementModule.loadCurrentUser()
      .then(current => {
        this.user = current;
      })
      .catch(err => {
        alert(err.message);
        AuthModule.authLogout();
      });
  }
}
</script>
<style scoped lang="sass">
.user-profile-container
  margin: 30px
  padding: 20px
  border-radius: 5px
  box-shadow: 0 10px 20px rgba(0,0,0,0.1), 0 6px 6px rgba(0,0,0,0.1)
</style>
