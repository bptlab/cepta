<template>
  <masonry-layout title="Dashboard">
    <!--<masonry-layout-tile :wrap="false" :layout-style="{'col-md-12' : true}">-->
    <!--<div class="row gap-20" style="overflow: hidden">-->
    <!-- Total Visits
        <div class="col-md-3">
          <div class="layers bd bgc-white p-20">
            <div class="layer w-100 mB-10">
              <h6 class="lh-1">Total Visits</h6>
            </div>
            <glance
              v-bind:samples="[
                20,
                30,
                10,
                50,
                30,
                20,
                4,
                73,
                12,
                23,
                56,
                23,
                56,
                23,
                45,
                68,
                12
              ]"
              :percent="50"
              fill-color="#3dafae"
              hover-color="#e700d2"
            />
          </div>
        </div>-->

    <!-- Total Page Views
        <div class="col-md-3">
          <div class="layers bd bgc-white p-20">
            <div class="layer w-100 mB-10">
              <h6 class="lh-1">Total Page Views</h6>
            </div>
            <glance
              v-bind:samples="[
                2,
                4,
                73,
                12,
                23,
                56,
                23,
                56,
                20,
                30,
                10,
                50,
                30,
                23,
                45,
                68,
                12
              ]"
              :percent="-7"
              fill-color="#e700d2"
              hover-color="#5e00ce"
            />
          </div>
        </div>-->

    <!-- Unique Visitors
        <div class="col-md-3">
          <div class="layers bd bgc-white p-20">
            <div class="layer w-100 mB-10">
              <h6 class="lh-1">Unique Visitor</h6>
            </div>
            <glance
              v-bind:samples="[12, 45, 4, 73, 12]"
              :percent="12"
              prefix="~"
              fill-color="#03a9f3"
              hover-color="#e5f315"
            />
          </div>
        </div>-->

    <!-- Bounce Rate
        <div class="col-md-3">
          <div class="layers bd bgc-white p-20">
            <div class="layer w-100 mB-10">
              <h6 class="lh-1">Bounce Rate</h6>
            </div>
            <glance
              v-bind:samples="[4, 73, 12, 12, 45, 4, 67]"
              :percent="33"
              fill-color="#f96262"
              hover-color="#9f0800"
              label-color="#03a9f3"
            />
          </div>
        </div>-->

    <!--</div>
    </masonry-layout-tile>-->

    <!-- Site Visits -->
    <masonry-layout-tile
      section="Site Visits"
      :layout-style="{ 'col-md-12': true }"
    >
      <div class="peer">
        <div class="layers">
          <div class="layer w-100">
            <!-- Progress Bars -->
            <div class="layers">
              <div class="layer w-100">
                <h5 class="mB-5">100k</h5>
                <small class="fw-600 c-grey-700">Visitors From USA</small>
                <span class="pull-right c-grey-600 fsz-sm">50%</span>
                <div class="progress mT-10">
                  <div
                    class="progress-bar bgc-deep-purple-500"
                    role="progressbar"
                    aria-valuenow="50"
                    aria-valuemin="0"
                    aria-valuemax="100"
                    style="width:50%;"
                  >
                    <span class="sr-only">50% Complete</span>
                  </div>
                </div>
              </div>
              <div class="layer w-100 mT-15">
                <h5 class="mB-5">1M</h5>
                <small class="fw-600 c-grey-700">Visitors From Europe</small>
                <span class="pull-right c-grey-600 fsz-sm">80%</span>
                <div class="progress mT-10">
                  <div
                    class="progress-bar bgc-green-500"
                    role="progressbar"
                    aria-valuenow="50"
                    aria-valuemin="0"
                    aria-valuemax="100"
                    style="width:80%;"
                  >
                    <span class="sr-only">80% Complete</span>
                  </div>
                </div>
              </div>
              <div class="layer w-100 mT-15">
                <h5 class="mB-5">450k</h5>
                <small class="fw-600 c-grey-700">Visitors From Australia</small>
                <span class="pull-right c-grey-600 fsz-sm">40%</span>
                <div class="progress mT-10">
                  <div
                    class="progress-bar bgc-light-blue-500"
                    role="progressbar"
                    aria-valuenow="50"
                    aria-valuemin="0"
                    aria-valuemax="100"
                    style="width:40%;"
                  >
                    <span class="sr-only">40% Complete</span>
                  </div>
                </div>
              </div>
              <div class="layer w-100 mT-15">
                <h5 class="mB-5">1B</h5>
                <small class="fw-600 c-grey-700">Visitors From India</small>
                <span class="pull-right c-grey-600 fsz-sm">90%</span>
                <div class="progress mT-10">
                  <div
                    class="progress-bar bgc-blue-grey-500"
                    role="progressbar"
                    aria-valuenow="50"
                    aria-valuemin="0"
                    aria-valuemax="100"
                    style="width:90%;"
                  >
                    <span class="sr-only">90% Complete</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Pie Charts -->
            <div class="peers pT-20 mT-20 bdT fxw-nw@lg+ jc-sb ta-c gap-10">
              <div class="peer">
                <tachometer-chart
                  :size="80"
                  :percent="75"
                  fillColor="#f44336"
                />
                <h6 class="fsz-sm">New Users</h6>
              </div>
              <div class="peer">
                <tachometer-chart
                  :size="80"
                  :percent="50"
                  fillColor="#2196f3"
                />
                <h6 class="fsz-sm">New Purchases</h6>
              </div>
              <div class="peer">
                <tachometer-chart
                  :size="80"
                  :percent="90"
                  fillColor="#ff9800"
                />
                <h6 class="fsz-sm">Bounce Rate</h6>
              </div>
            </div>
          </div>
        </div>
      </div>
    </masonry-layout-tile>

    <!--
    <div class="masonry-item col-12">

      <div class="bd bgc-white">
        <div class="peers fxw-nw@lg+ ai-s">
          <div class="peer peer-greed w-70p@lg+ w-100@lg- p-20">
            <div class="layer w-100">
              <h6 class="lh-1">Site Visits</h6>
            </div>
          </div>
          <div class="peer bdL p-20 w-30p@lg+ w-100p@lg-">
            <div class="layers">
              <div class="layer w-100">

                <div class="layers">
                  <div class="layer w-100">
                    <h5 class="mB-5">100k</h5>
                    <small class="fw-600 c-grey-700">Visitors From USA</small>
                    <span class="pull-right c-grey-600 fsz-sm">50%</span>
                    <div class="progress mT-10">
                      <div
                        class="progress-bar bgc-deep-purple-500"
                        role="progressbar"
                        aria-valuenow="50"
                        aria-valuemin="0"
                        aria-valuemax="100"
                        style="width:50%;"
                      >
                        <span class="sr-only">50% Complete</span>
                      </div>
                    </div>
                  </div>
                  <div class="layer w-100 mT-15">
                    <h5 class="mB-5">1M</h5>
                    <small class="fw-600 c-grey-700"
                      >Visitors From Europe</small
                    >
                    <span class="pull-right c-grey-600 fsz-sm">80%</span>
                    <div class="progress mT-10">
                      <div
                        class="progress-bar bgc-green-500"
                        role="progressbar"
                        aria-valuenow="50"
                        aria-valuemin="0"
                        aria-valuemax="100"
                        style="width:80%;"
                      >
                        <span class="sr-only">80% Complete</span>
                      </div>
                    </div>
                  </div>
                  <div class="layer w-100 mT-15">
                    <h5 class="mB-5">450k</h5>
                    <small class="fw-600 c-grey-700"
                      >Visitors From Australia</small
                    >
                    <span class="pull-right c-grey-600 fsz-sm">40%</span>
                    <div class="progress mT-10">
                      <div
                        class="progress-bar bgc-light-blue-500"
                        role="progressbar"
                        aria-valuenow="50"
                        aria-valuemin="0"
                        aria-valuemax="100"
                        style="width:40%;"
                      >
                        <span class="sr-only">40% Complete</span>
                      </div>
                    </div>
                  </div>
                  <div class="layer w-100 mT-15">
                    <h5 class="mB-5">1B</h5>
                    <small class="fw-600 c-grey-700">Visitors From India</small>
                    <span class="pull-right c-grey-600 fsz-sm">90%</span>
                    <div class="progress mT-10">
                      <div
                        class="progress-bar bgc-blue-grey-500"
                        role="progressbar"
                        aria-valuenow="50"
                        aria-valuemin="0"
                        aria-valuemax="100"
                        style="width:90%;"
                      >
                        <span class="sr-only">90% Complete</span>
                      </div>
                    </div>
                  </div>
                </div>


                <div class="peers pT-20 mT-20 bdT fxw-nw@lg+ jc-sb ta-c gap-10">
                  <div class="peer">
                    <tacho-chart :size="80" :percent="75" fillColor="#f44336" />
                    <h6 class="fsz-sm">New Users</h6>
                  </div>
                  <div class="peer">
                    <tacho-chart :size="80" :percent="50" fillColor="#2196f3" />
                    <h6 class="fsz-sm">New Purchases</h6>
                  </div>
                  <div class="peer">
                    <tacho-chart :size="80" :percent="90" fillColor="#ff9800" />
                    <h6 class="fsz-sm">Bounce Rate</h6>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    -->

    <masonry-layout-tile section="Monthly Stats">
      <div class="layer">
        <line-chart
          :height="200"
          :chart-data="lineChartData"
          :options="{ legend: { display: false } }"
        />
      </div>
      <div class="layer mT-10">
        <div class="peers ai-c jc-c gapX-20">
          <div class="peer">
            <span class="fsz-def fw-600 mR-8 c-grey-800"
              >10% <i class="icon-arrow-up c-green-500"></i>
            </span>
            <small class="c-grey-500 fw-600">APPL</small>
          </div>
          <div class="peer fw-600">
            <span class="fsz-def fw-600 mR-8 c-grey-800"
              >2% <i class="icon-arrow-down c-red-500"></i
            ></span>
            <small class="c-grey-500 fw-600">Average</small>
          </div>
          <div class="peer fw-600">
            <span class="fsz-def fw-600 mR-8 c-grey-800"
              >15% <i class="icon-arrow-up c-green-500"></i
            ></span>
            <small class="c-grey-500 fw-600">Sales</small>
          </div>
          <div class="peer fw-600">
            <span class="fsz-def fw-600 mR-8 c-grey-800"
              >8% <i class="icon-arrow-down c-red-500"></i
            ></span>
            <small class="c-grey-500 fw-600">Profit</small>
          </div>
        </div>
      </div>
    </masonry-layout-tile>

    <!--
    <div class="masonry-item col-md-6">

      <div class="bd bgc-white">
        <div class="layers">
          <div class="layer w-100 pX-20 pT-20">
            <h6 class="lh-1">Monthly Stats</h6>
          </div>
          <div class="layer w-100 p-20">
            <line-chart
              :height="200"
              :chart-data="lineChartData"
              :options="{ legend: { display: false } }"
            />
          </div>
          <div class="layer bdT p-20 w-100">
            <div class="peers ai-c jc-c gapX-20">
              <div class="peer">
                <span class="fsz-def fw-600 mR-8 c-grey-800"
                  >10% <i class="icon-arrow-up c-green-500"
                /></span>
                <small class="c-grey-500 fw-600">APPL</small>
              </div>
              <div class="peer fw-600">
                <span class="fsz-def fw-600 mR-8 c-grey-800"
                  >2% <i class="icon-arrow-down c-red-500"></i
                ></span>
                <small class="c-grey-500 fw-600">Average</small>
              </div>
              <div class="peer fw-600">
                <span class="fsz-def fw-600 mR-8 c-grey-800"
                  >15% <i class="icon-arrow-up c-green-500"></i
                ></span>
                <small class="c-grey-500 fw-600">Sales</small>
              </div>
              <div class="peer fw-600">
                <span class="fsz-def fw-600 mR-8 c-grey-800"
                  >8% <i class="icon-arrow-down c-red-500"></i
                ></span>
                <small class="c-grey-500 fw-600">Profit</small>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    -->

    <!--
    <div class="masonry-item col-md-6">
      <div class="bd bgc-white p-20">
        <div class="layers">
          <div class="layer w-100 mB-10">
            <h6 class="lh-1">Todo List</h6>
          </div>
          <todo-list v-bind:tasks="todoListTasks" />
        </div>
      </div>
    </div>
    -->

    <!--
    <div class="masonry-item col-md-6">
      <div class="bd bgc-white">
        <div class="layers">
          <div class="layer w-100 p-20">
            <h6 class="lh-1">Sales Report</h6>
          </div>
          <sales-report v-bind:sales="sales" />
        </div>
        <div class="ta-c bdT w-100 p-20">
          <a href="#sales">Check all the sales</a>
        </div>
      </div>
    </div>
    -->

    <!--
    <div class="masonry-item col-md-6">

      <div class="bd bgc-white">
        <div class="layers">
          <div class="layer w-100 p-20">
            <h6 class="lh-1">Quick Chat</h6>
          </div>
          <div class="layer w-100">

            <div class="bgc-grey-200 p-20 gapY-15">

              <div class="peers fxw-nw">
                <div class="peer mR-20">
                  <img
                    class="w-2r bdrs-50p"
                    src="https://randomuser.me/api/portraits/men/11.jpg"
                    alt=""
                  />
                </div>
                <div class="peer peer-greed">
                  <div class="layers ai-fs gapY-5">
                    <div class="layer">
                      <div
                        class="peers fxw-nw ai-c pY-3 pX-10 bgc-white bdrs-2 lh-3/2"
                      >
                        <div class="peer mR-10">
                          <small>10:00 AM</small>
                        </div>
                        <div class="peer-greed">
                          <span>Lorem Ipsum is simply dummy text of</span>
                        </div>
                      </div>
                    </div>
                    <div class="layer">
                      <div
                        class="peers fxw-nw ai-c pY-3 pX-10 bgc-white bdrs-2 lh-3/2"
                      >
                        <div class="peer mR-10">
                          <small>10:00 AM</small>
                        </div>
                        <div class="peer-greed">
                          <span>the printing and typesetting industry.</span>
                        </div>
                      </div>
                    </div>
                    <div class="layer">
                      <div
                        class="peers fxw-nw ai-c pY-3 pX-10 bgc-white bdrs-2 lh-3/2"
                      >
                        <div class="peer mR-10">
                          <small>10:00 AM</small>
                        </div>
                        <div class="peer-greed">
                          <span>Lorem Ipsum has been the industry's</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>


              <div class="peers fxw-nw ai-fe">
                <div class="peer ord-1 mL-20">
                  <img
                    class="w-2r bdrs-50p"
                    src="https://randomuser.me/api/portraits/men/12.jpg"
                    alt=""
                  />
                </div>
                <div class="peer peer-greed ord-0">
                  <div class="layers ai-fe gapY-10">
                    <div class="layer">
                      <div
                        class="peers fxw-nw ai-c pY-3 pX-10 bgc-white bdrs-2 lh-3/2"
                      >
                        <div class="peer mL-10 ord-1">
                          <small>10:00 AM</small>
                        </div>
                        <div class="peer-greed ord-0">
                          <span>Heloo</span>
                        </div>
                      </div>
                    </div>
                    <div class="layer">
                      <div
                        class="peers fxw-nw ai-c pY-3 pX-10 bgc-white bdrs-2 lh-3/2"
                      >
                        <div class="peer mL-10 ord-1">
                          <small>10:00 AM</small>
                        </div>
                        <div class="peer-greed ord-0">
                          <span>??</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="p-20 bdT bgc-white">
              <div class="pos-r">
                <input
                  type="text"
                  class="form-control bdrs-10em m-0"
                  placeholder="Say something..."
                />
                <button
                  type="button"
                  class="btn btn-primary bdrs-50p w-2r p-0 h-2r pos-a r-1 t-1"
                >
                  <i class="fas fa-paper-plane"></i>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>-->
  </masonry-layout>
</template>

<script>
import { COLORS } from "@/colors";

import MasonryLayout from "@/components/MasonryLayout";
import MasonryLayoutTile from "@/components/MasonryLayoutTile";

export default {
  name: "Dashboard",
  components: {
    MasonryLayoutTile,
    MasonryLayout,

  },
  data() {
    return {
      sales: {
        title: "November 2017",
        total: {
          value: 6000,
          currency: "$"
        },
        items: [
          {
            name: "Item #1",
            status: "Unavailable",
            date: "Nov 18",
            price: 12,
            _currency: "$"
          },
          {
            name: "Item #2",
            status: "New",
            date: "Nov 19",
            price: 34,
            _currency: "$"
          },
          {
            name: "Item #3",
            status: "New",
            date: "Nov 20",
            price: -45,
            _currency: "$"
          },
          {
            name: "Item #4",
            status: "Unavailable",
            date: "Nov 21",
            price: 65,
            _currency: "$"
          },
          {
            name: "Item #5",
            status: "Used",
            date: "Nov 22",
            price: 78,
            _currency: "$"
          },
          {
            name: "Item #6",
            status: "Used",
            date: "Nov 23",
            price: -88,
            _currency: "$"
          },
          {
            name: "Item #7",
            status: "Old",
            date: "Nov 24",
            price: 56,
            _currency: "$"
          }
        ]
      },
      todoListTasks: [
        {
          description: "Call John for Dinner",
          priority: 1,
          label: null,
          done: true
        },
        {
          description: "Book Boss Flight",
          priority: 2,
          label: "2 Days",
          done: false
        },
        {
          description: "Hit the Gym",
          priority: 3,
          label: "3 Minutes",
          done: false
        },
        {
          description: "Give Purchase Report",
          priority: 1,
          label: "not important",
          done: false
        },
        {
          description: "Watch Game of Thrones Episode",
          priority: 0,
          label: "Tomorrow",
          done: false
        }
      ],
      lineChartData: {
        labels: [
          "January",
          "February",
          "March",
          "April",
          "May",
          "June",
          "July"
        ],
        datasets: [
          {
            label: "Series A",
            backgroundColor: "rgba(237, 231, 246, 0.5)",
            borderColor: COLORS["deep-purple-500"],
            pointBackgroundColor: COLORS["deep-purple-700"],
            borderWidth: 2,
            data: [60, 50, 70, 60, 50, 70, 60]
          },
          {
            label: "Series B",
            backgroundColor: "rgba(232, 245, 233, 0.5)",
            borderColor: COLORS["blue-500"],
            pointBackgroundColor: COLORS["blue-700"],
            borderWidth: 2,
            data: [70, 75, 85, 70, 75, 85, 70]
          }
        ]
      },
      chat: {
        pete: {
          fullname: "Peter",
          avatar: "https://randomuser.me/api/portraits/men/11.jpg",
          chat: [
            {
              timestamp: new Date("July 20, 2016 15:00:00").getTime(),
              message: "Lorem Ipsum is simply dummy text of"
            }
          ]
        },
        anton: {
          fullname: "Anton",
          avatar: "https://randomuser.me/api/portraits/women/11.jpg",
          chat: [
            {
              timestamp: new Date("July 20, 2016 15:10:12").getTime(),
              message: "Lorem Ipsum has been the industry's"
            },
            {
              timestamp: new Date("July 20, 2016 16:14:59").getTime(),
              message: "Amazing!"
            },
            {
              timestamp: new Date("July 20, 2016 15:14:59").getTime(),
              message: "Sounds great!"
            }
          ]
        }
      }
    };
  },
  mounted() {}
};
</script>
