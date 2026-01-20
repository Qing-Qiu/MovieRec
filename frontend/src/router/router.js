import {createRouter, createWebHistory} from 'vue-router';
import LoginPage from '../views/LoginPage';
import HomePage from '../views/HomePage';
import RegisterPage from '../views/RegisterPage';
import DynamicMovie from '../components/DynamicMovie';
import MovieCenterMod from "../components/MovieCenterMod";
import RecommendSearchMod from "../components/RecommendSearchMod";
import ModelMod from "../components/ModelMod";
import ChartMod from "../components/ChartMod";
import DynamicPerson from "@/components/DynamicPerson";
import MusicMod from "@/components/MusicMod";

const routes = [
    {
        path: '/auth/login',
        component: LoginPage,
        meta: {
            dependent: true,
        }
    },
    {
        path: '/auth/register',
        component: RegisterPage,
        meta: {
            dependent: true,
        }
    },
    {
        path: '/',
        redirect: '/auth/login',
    },
    {
        path: '/home',
        name: 'Home',
        component: () => import('../components/RecommendSearchMod'),
        meta: {
            dependent: false,
            selects: ['1'],
        }
    },
    {
        path: '/center',
        component: MovieCenterMod,
        meta: {
            dependent: false,
            selects: ['2'],
        }
    },
    {
        path: '/model',
        component: ModelMod,
        meta: {
            dependent: false,
            selects: ['3-1'],
        }
    },
    {
        path: '/chart',
        component: ChartMod,
        meta: {
            dependent: false,
            selects: ['3-2'],
        }
    },
    {
        path: '/music',
        component: MusicMod,
        meta: {
            dependent: false,
            selects: ['3-3'],
        }
    },
    {
        path: '/movie/:movie_id',
        name: 'DynamicMovie',
        component: DynamicMovie,
        props: true,
        meta: {
            dependent: false,
            selects: ['1'],
        }
    },
    {
        path: '/person/:person_id',
        name: 'DynamicPerson',
        component: DynamicPerson,
        props: true,
        meta: {
            dependent: false,
            selects: ['1'],
        }
    },
    {
        path: '/:path(.*)',
        name: 'NotFoundPage',
        component: () => import('../views/NotFoundPage'),
        meta: {
            dependent: true,
        }
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

export default router;
