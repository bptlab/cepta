<template>
  <div class="users-container">
    <masonry-layout>
      <masonry-layout-tile
        section="Statistics"
        class="stats"
        layoutStyle="col-md-12"
      >
        <!-- Pie Charts -->
        <div class="peers jc-sb ta-c">
          <div class="metric peer">
            <p>Registered Users</p>
            <p class="metric-value">{{ totalUsers }}</p>
          </div>
        </div>
      </masonry-layout-tile>

      <masonry-layout-tile
        class="transports-overview"
        section="Create new user"
        layoutStyle="col-md-12"
      >
        <edit-user-form
          v-on:create="createUser"
          :show-create-button="true"
        ></edit-user-form>
      </masonry-layout-tile>
    </masonry-layout>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import MasonryLayout from "../components/MasonryLayout.vue";
import MasonryLayoutTile from "../components/MasonryLayoutTile.vue";
import EditUserForm from "../components/EditUserForm.vue";
import { UserManagementModule } from "../store/modules/usermgmt";
import { AddUserRequest } from "../generated/protobuf/models/grpc/usermgmt_pb";

@Component({
  name: "Users",
  components: {
    MasonryLayout,
    MasonryLayoutTile,
    EditUserForm
  }
})
export default class Users extends Vue {
  protected totalUsers = 0;

  createUser(req: AddUserRequest) {
    debugger;
    UserManagementModule.addUser(req)
      .then(_ => {
        alert("Added new user!");
      })
      .catch(err => {
        alert(`Failed to create user profile: ${err.message}`);
      });
  }

  mounted() {
    // TODO: Load total numbers of users
  }
}
</script>

<style lang="sass">
.users-container
  width: 100%

  .metric
    p
      text-align: left
      vertical-align: top
      font-size: 1rem

      &.metric-value
        font-size: 2.5rem
</style>
